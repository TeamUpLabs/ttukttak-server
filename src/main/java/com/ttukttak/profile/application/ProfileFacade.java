package com.ttukttak.profile.application;

import com.ttukttak.auth.presentation.common.UserSummary;
import com.ttukttak.common.OffsetCursor;
import com.ttukttak.domain.feedback.application.query.FeedbackQueryService;
import com.ttukttak.domain.feedback.domain.Feedback;
import com.ttukttak.domain.profile.domain.Badge;
import com.ttukttak.domain.profile.domain.StreakGrid;
import com.ttukttak.domain.submission.application.query.SubmissionQueryService;
import com.ttukttak.domain.submission.domain.Streak;
import com.ttukttak.domain.submission.domain.Submission;
import com.ttukttak.domain.user.application.command.UserCommandService;
import com.ttukttak.domain.user.application.query.UserQueryService;
import com.ttukttak.domain.user.domain.User;
import com.ttukttak.domain.week.application.query.WeekQueryService;
import com.ttukttak.domain.week.domain.Phase;
import com.ttukttak.domain.week.domain.Week;
import com.ttukttak.profile.presentation.response.*;
import com.ttukttak.submission.application.SubmissionFacade;
import com.ttukttak.submission.presentation.response.SubmissionCard;
import com.ttukttak.week.presentation.common.WeekSchedule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/** 요청·응답 DTO와 도메인 사이를 이어줘요. 트랜잭션은 각 서비스가 가져요. */
// ponytail: 프로필을 볼 때마다 그 사람의 기록을 다 읽어서 계산해요. 느려지면 통계를 users에 캐시해요.
@Component
@RequiredArgsConstructor
public class ProfileFacade {
    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;
    private final WeekQueryService weekQueryService;
    private final SubmissionQueryService submissionQueryService;
    private final FeedbackQueryService feedbackQueryService;
    private final SubmissionFacade submissionFacade;

    public ProfileResponse get(String handle, UUID viewerId) {
        History history = history(userQueryService.get(handle), Instant.now());
        UUID id = history.user().getId();
        Set<String> grid = weekQueryService.opened(history.now(), StreakGrid.WEEKS).stream()
                .map(Week::getId).collect(Collectors.toSet());
        return new ProfileResponse(
                ProfileResponse.Profile.from(history.user()),
                new ProfileResponse.Stats(history.streak().current(), history.streak().best(),
                        history.submitted().stream().filter(grid::contains).count(), history.top3().size(),
                        submissionQueryService.votesReceived(id), feedbackQueryService.createdAtByUser(id).size()),
                new ProfileResponse.Social(userQueryService.followers(id), userQueryService.following(id)),
                viewerId == null ? null : new ProfileResponse.Viewer(
                        userQueryService.isFollowing(viewerId, id), viewerId.equals(id)));
    }

    public StreakResponse streak(String handle) {
        History history = history(userQueryService.get(handle), Instant.now());
        List<Week> weeks = new ArrayList<>(weekQueryService.opened(history.now(), StreakGrid.WEEKS));
        Collections.reverse(weeks);
        return new StreakResponse(StreakGrid.of(weeks, history.submitted(), history.top3(), history.now()),
                new StreakResponse.Current(history.streak().current(), history.streak().since()));
    }

    public BadgeListResponse badges(String handle) {
        History history = history(userQueryService.get(handle), Instant.now());
        Map<String, Instant> submittedAt = history.submissions().stream()
                .collect(Collectors.toMap(Submission::getWeekId, Submission::getSubmittedAt));
        List<Instant> podiumAt = history.themes().stream().filter(week -> history.top3().contains(week.getId()))
                .map(Week::getResultsAt).sorted().toList();
        return BadgeListResponse.from(Badge.of(history.themes().reversed(), submittedAt, podiumAt,
                feedbackQueryService.createdAtByUser(history.user().getId())));
    }

    public ProfileSubmissionListResponse submissions(String handle, String cursor, int limit, UUID viewerId) {
        History history = history(userQueryService.get(handle), Instant.now());
        OffsetCursor at = OffsetCursor.decode(cursor);
        List<Submission> all = history.submissions();
        OffsetCursor.Slice<Submission> slice = at.slice(limit,
                n -> all.subList(Math.min(at.offset(), all.size()), Math.min(at.offset() + n, all.size())));
        List<Submission> page = slice.items();
        Map<UUID, Submission> byId = page.stream().collect(Collectors.toMap(Submission::getId, Function.identity()));
        List<ProfileSubmissionListResponse.Item> items = new ArrayList<>();
        for (SubmissionCard card : submissionFacade.cards(page, viewerId)) {
            Submission submission = byId.get(card.id());
            Week week = weekQueryService.get(submission.getWeekId());
            boolean medal = history.top3().contains(week.getId());
            items.add(new ProfileSubmissionListResponse.Item(card,
                    new ProfileSubmissionListResponse.WeekRef(week.getCode(), week.getTitle()),
                    medal ? new ProfileSubmissionListResponse.Medal(card.track(), history.ranks().get(card.id())) : null));
        }
        return new ProfileSubmissionListResponse(items, slice.next());
    }

    public ProfileFeedbackListResponse feedback(String handle, String cursor, int limit) {
        User user = userQueryService.get(handle);
        OffsetCursor at = OffsetCursor.decode(cursor);
        OffsetCursor.Slice<Feedback> slice = at.slice(limit, n -> feedbackQueryService.byUser(user.getId(), at.offset(), n));
        List<Feedback> page = slice.items();

        Map<UUID, Submission> submissions = submissionQueryService.getAll(
                        page.stream().map(Feedback::getSubmissionId).distinct().toList())
                .stream().collect(Collectors.toMap(Submission::getId, Function.identity()));
        Map<UUID, User> authors = userQueryService.getAll(
                submissions.values().stream().map(Submission::getUserId).distinct().toList());
        Map<UUID, Long> helpful = feedbackQueryService.helpfulCounts(page.stream().map(Feedback::getId).toList());
        // 쿼리가 이미 걸렀어요. 그 사이에 철회·탈퇴한 것만 남을 수 있어 한 번 더 봐요.
        List<ProfileFeedbackListResponse.Item> items = page.stream()
                .filter(f -> submissions.containsKey(f.getSubmissionId())
                        && authors.containsKey(submissions.get(f.getSubmissionId()).getUserId()))
                .map(f -> {
                    Submission submission = submissions.get(f.getSubmissionId());
                    return new ProfileFeedbackListResponse.Item(f.getId(), f.getType(), f.getBody(),
                            helpful.getOrDefault(f.getId(), 0L), WeekSchedule.kst(f.getCreatedAt()),
                            new ProfileFeedbackListResponse.SubmissionRef(submission.getId(),
                                    submission.getContent().title(), UserSummary.from(authors.get(submission.getUserId()))));
                })
                .toList();
        return new ProfileFeedbackListResponse(items, slice.next());
    }

    public FollowResponse follow(String handle, UUID followerId) {
        return new FollowResponse(true, userCommandService.follow(followerId, handle));
    }

    public FollowResponse unfollow(String handle, UUID followerId) {
        return new FollowResponse(false, userCommandService.unfollow(followerId, handle));
    }

    /**
     * 프로필 계산에 쓰는 그 사람의 기록.
     * themes: 열린 주제 주(최신순), top3: 결과 발표 뒤 트랙 3위 안에 든 주차, ranks: 제출작 id → 트랙 안 순위
     */
    private record History(User user, Instant now, List<Week> themes, List<Submission> submissions,
                           Set<String> submitted, Set<String> top3, Map<UUID, Integer> ranks, Streak streak) {
    }

    private History history(User user, Instant now) {
        List<Week> themes = weekQueryService.openedThemes(now);
        Map<String, Week> weeks = themes.stream().collect(Collectors.toMap(Week::getId, Function.identity()));
        List<Submission> submissions = submissionQueryService.byUser(user.getId());
        Set<String> submitted = submissions.stream().map(Submission::getWeekId).collect(Collectors.toSet());
        Map<UUID, Integer> ranks = submissionQueryService.trackRanks(user.getId());
        Set<String> top3 = submissions.stream()
                .filter(s -> ranks.getOrDefault(s.getId(), Integer.MAX_VALUE) <= 3)
                .filter(s -> weeks.containsKey(s.getWeekId()) && weeks.get(s.getWeekId()).phase(now) == Phase.CLOSED)
                .map(Submission::getWeekId).collect(Collectors.toSet());
        return new History(user, now, themes, submissions, submitted, top3, ranks, Streak.of(themes, submitted, now));
    }
}
