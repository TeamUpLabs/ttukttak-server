package com.ttukttak.submission.application;

import com.ttukttak.common.OffsetCursor;
import com.ttukttak.domain.feedback.application.query.FeedbackQueryService;
import com.ttukttak.domain.submission.application.command.SubmissionCommandService;
import com.ttukttak.domain.submission.application.query.SubmissionQueryService;
import com.ttukttak.domain.submission.domain.Scoreboard;
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
import org.springframework.stereotype.Component;

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
    private final FeedbackQueryService feedbackQueryService;

    /** sort가 없으면 제작 기간엔 무작위(먼저 낸 사람이 유리하지 않게), 이후엔 투표순 */
    public SubmissionListResponse list(String weekId, Track track, SubmissionSort sort, UUID exclude,
                                       String cursor, int limit, UUID viewerId) {
        Week week = weekQueryService.get(weekId);
        SubmissionSort order = sort != null ? sort
                : week.phase(Instant.now()) == Phase.BUILDING ? SubmissionSort.RANDOM : SubmissionSort.VOTES;
        // ponytail: 명세는 세션마다 같은 순서지만, 비로그인에 세션을 만들지 않으려고 첫 페이지마다 새로 섞어요.
        OffsetCursor at = OffsetCursor.decode(cursor);
        int size = Math.clamp(limit, 1, MAX_LIMIT);
        List<Submission> rows = submissionQueryService.page(weekId, track, exclude, order, at.seed(), at.offset(), size + 1);
        boolean hasNext = rows.size() > size;
        List<Submission> page = hasNext ? rows.subList(0, size) : rows;
        return new SubmissionListResponse(
                cards(page, week, viewerId),
                hasNext ? at.next(size).encode() : null,
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

    public void hide(UUID id, String reason) {
        submissionCommandService.hide(id, reason);
    }

    public VoteResponse vote(UUID id, UUID userId) {
        return new VoteResponse(true, submissionCommandService.vote(id, userId, Instant.now()));
    }

    public VoteResponse unvote(UUID id, UUID userId) {
        return new VoteResponse(false, submissionCommandService.unvote(id, userId, Instant.now()));
    }

    /** 주차 결과의 트랙별 상위 3개. 결과 발표 전엔 null이에요. */
    public Podium podium(Week week, UUID viewerId) {
        if (week.phase(Instant.now()) != Phase.CLOSED) return null;
        Scoreboard scoreboard = submissionQueryService.scoreboard(week.getId());
        Function<Track, List<SubmissionCard>> top = track -> cards(
                scoreboard.podium(track).stream().map(submissionQueryService::get).toList(), week, scoreboard, viewerId);
        return new Podium(top.apply(Track.BEGINNER), top.apply(Track.REGULAR), top.apply(Track.EXPERT));
    }

    // 탈퇴한 사용자의 제출작은 없는 것으로 봐요.
    private SubmissionDetailResponse detail(Submission submission, UUID viewerId, Instant now) {
        Week week = weekQueryService.get(submission.getWeekId());
        List<SubmissionCard> card = cards(List.of(submission), week, viewerId);
        if (card.isEmpty()) throw new SubmissionNotFoundException();
        User author = userQueryService.get(submission.getUserId());
        return SubmissionDetailResponse.of(card.getFirst(), submission, week,
                submissionQueryService.count(week.getId(), null, null), feedbackQueryService.count(submission.getId()),
                author, submissionQueryService.streak(author.getId(), now).current(), now);
    }

    /** 여러 주차에 걸친 제출작 카드. 순서는 그대로예요. 작성자가 탈퇴한 건 빠져요. */
    public List<SubmissionCard> cards(List<Submission> submissions, UUID viewerId) {
        Map<String, List<SubmissionCard>> byWeek = new HashMap<>();
        submissions.stream().map(Submission::getWeekId).distinct().forEach(weekId -> byWeek.put(weekId,
                cards(submissions.stream().filter(s -> s.getWeekId().equals(weekId)).toList(),
                        weekQueryService.get(weekId), viewerId)));
        Map<UUID, SubmissionCard> byId = byWeek.values().stream().flatMap(List::stream)
                .collect(Collectors.toMap(SubmissionCard::id, Function.identity()));
        return submissions.stream().map(s -> byId.get(s.getId())).filter(Objects::nonNull).toList();
    }

    private List<SubmissionCard> cards(List<Submission> submissions, Week week, UUID viewerId) {
        return cards(submissions, week, submissionQueryService.scoreboard(week.getId()), viewerId);
    }

    // 작성자가 탈퇴했으면 빼요.
    private List<SubmissionCard> cards(List<Submission> submissions, Week week, Scoreboard scoreboard, UUID viewerId) {
        List<UUID> ids = submissions.stream().map(Submission::getId).toList();
        Map<UUID, User> authors = userQueryService.getAll(submissions.stream().map(Submission::getUserId).toList())
                .stream().collect(Collectors.toMap(User::getId, Function.identity()));
        Set<UUID> voted = submissionQueryService.voted(viewerId, ids);
        boolean showRank = week.isVotingOver(Instant.now());
        return submissions.stream().filter(s -> authors.containsKey(s.getUserId()))
                .map(s -> SubmissionCard.of(s, authors.get(s.getUserId()), scoreboard.get(s.getId()), showRank,
                        viewerId, voted.contains(s.getId())))
                .toList();
    }
}
