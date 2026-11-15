package com.ttukttak.submission.application;

import com.ttukttak.common.exception.BusinessException;
import com.ttukttak.domain.submission.application.command.SubmissionCommandService;
import com.ttukttak.domain.submission.application.query.SubmissionQueryService;
import com.ttukttak.domain.submission.domain.Streak;
import com.ttukttak.domain.submission.domain.Submission;
import com.ttukttak.domain.submission.domain.SubmissionSort;
import com.ttukttak.domain.submission.domain.Track;
import com.ttukttak.domain.submission.exception.AlreadySubmittedException;
import com.ttukttak.domain.submission.exception.SubmissionNotFoundException;
import com.ttukttak.domain.user.application.query.UserQueryService;
import com.ttukttak.domain.user.domain.User;
import com.ttukttak.domain.week.application.query.WeekQueryService;
import com.ttukttak.domain.week.domain.Phase;
import com.ttukttak.domain.week.domain.Week;
import com.ttukttak.submission.presentation.request.SubmitRequest;
import com.ttukttak.submission.presentation.request.UpdateSubmissionRequest;
import com.ttukttak.submission.presentation.response.*;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/** 요청·응답 DTO와 도메인 사이를 이어줘요. 트랜잭션은 각 서비스가 가져요. */
@Component
@RequiredArgsConstructor
public class SubmissionFacade {
    private static final int MAX_LIMIT = 50;

    private final SubmissionCommandService submissionCommandService;
    private final SubmissionQueryService submissionQueryService;
    private final WeekQueryService weekQueryService;
    private final UserQueryService userQueryService;

    /** sort가 없으면 제작 기간엔 무작위(먼저 낸 사람이 유리하지 않게), 이후엔 투표순 */
    public SubmissionListResponse list(String weekId, Track track, SubmissionSort sort, UUID exclude,
                                       String cursor, int limit, UUID viewerId) {
        Week week = weekQueryService.get(weekId);
        SubmissionSort order = sort != null ? sort
                : week.phase(Instant.now()) == Phase.BUILDING ? SubmissionSort.RANDOM : SubmissionSort.VOTES;
        Cursor at = Cursor.decode(cursor);
        int size = Math.clamp(limit, 1, MAX_LIMIT);
        List<Submission> rows = submissionQueryService.page(weekId, track, exclude, order, at.seed(), at.offset(), size + 1);
        boolean hasNext = rows.size() > size;
        List<Submission> page = hasNext ? rows.subList(0, size) : rows;
        Map<UUID, User> authors = userQueryService.getAll(page.stream().map(Submission::getUserId).toList())
                .stream().collect(Collectors.toMap(User::getId, Function.identity()));
        return new SubmissionListResponse(
                page.stream().map(s -> SubmissionCard.of(s, authors.get(s.getUserId()), viewerId)).toList(),
                hasNext ? new Cursor(at.offset() + size, at.seed()).encode() : null,
                submissionQueryService.count(weekId, track, exclude));
    }

    public SubmissionDetailResponse get(UUID id, UUID viewerId) {
        return detail(submissionQueryService.get(id), viewerId, Instant.now());
    }

    public SubmitResponse submit(String weekId, UUID userId, SubmitRequest request) {
        Instant now = Instant.now();
        Streak before = submissionQueryService.streak(userId, now);
        Submission saved;
        try {
            saved = submissionCommandService.submit(weekId, userId, request.toContent(), now);
        } catch (DataIntegrityViolationException e) {
            // 동시에 두 번 내면 늦은 쪽이 고유 제약에 걸려요. 롤백된 트랜잭션 밖에서 먼저 저장된 걸 찾아 알려줘요.
            throw submissionQueryService.find(weekId, userId)
                    .map(existing -> (RuntimeException) new AlreadySubmittedException(existing.getId()))
                    .orElse(e);
        }
        Streak after = submissionQueryService.streak(userId, now);
        return new SubmitResponse(detail(saved, userId, now).submission(),
                new SubmitResponse.Streak(after.current(), after.best(), after.best() > before.best()));
    }

    public SubmissionDetailResponse update(UUID id, UUID userId, UpdateSubmissionRequest request) {
        Instant now = Instant.now();
        return detail(submissionCommandService.update(id, userId, request.toPatch(), now), userId, now);
    }

    public void withdraw(UUID id, UUID userId) {
        submissionCommandService.withdraw(id, userId, Instant.now());
    }

    // 탈퇴한 사용자의 제출작은 없는 것으로 봐요.
    private SubmissionDetailResponse detail(Submission submission, UUID viewerId, Instant now) {
        User author = userQueryService.getAll(List.of(submission.getUserId())).stream().findFirst()
                .orElseThrow(SubmissionNotFoundException::new);
        Week week = weekQueryService.get(submission.getWeekId());
        return SubmissionDetailResponse.of(submission, week, submissionQueryService.count(week.getId(), null, null),
                author, submissionQueryService.streak(author.getId(), now).current(), viewerId, now);
    }

    /** "몇 번째부터:무작위 씨앗"을 base64로. 무작위 순서가 페이지를 넘겨도 섞이지 않게 씨앗을 이어받아요. */
    // ponytail: 명세는 세션마다 같은 순서지만, 비로그인에 세션을 만들지 않으려고 첫 페이지마다 새로 섞어요.
    private record Cursor(int offset, String seed) {
        static Cursor decode(String cursor) {
            if (cursor == null) return new Cursor(0, UUID.randomUUID().toString().substring(0, 8));
            try {
                String[] parts = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8).split(":", 2);
                int offset = Integer.parseInt(parts[0]);
                if (offset < 0 || parts.length < 2) throw new IllegalArgumentException();
                return new Cursor(offset, parts[1]);
            } catch (IllegalArgumentException e) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "invalid_request", "목록 위치가 올바르지 않아요.");
            }
        }

        String encode() {
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString((offset + ":" + seed).getBytes(StandardCharsets.UTF_8));
        }
    }
}
