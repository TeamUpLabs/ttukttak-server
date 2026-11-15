package com.ttukttak.submission.presentation.response;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.ttukttak.auth.presentation.common.UserSummary;
import com.ttukttak.domain.submission.domain.Story;
import com.ttukttak.domain.submission.domain.Submission;
import com.ttukttak.domain.user.domain.User;
import com.ttukttak.domain.week.domain.Week;
import com.ttukttak.week.presentation.common.WeekSchedule;
import com.ttukttak.week.presentation.response.WeekSummary;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;

/** GET /submissions/:id 응답 */
public record SubmissionDetailResponse(Detail submission, WeekSummary week, Author author, Checks checks) {

    // ponytail: feedbackCount는 피드백을 만들 때 채워요.
    public record Detail(@JsonUnwrapped SubmissionCard card, String demoUrl, String repoUrl, Story story,
                         boolean demoWithoutLogin, OffsetDateTime updatedAt, long workHours, int feedbackCount) {

        /** workHours: 주제 공개부터 제출까지 걸린 시간(반올림) */
        public static Detail of(SubmissionCard card, Submission submission, Week week) {
            var content = submission.getContent();
            long minutes = Duration.between(week.getOpensAt(), submission.getSubmittedAt()).toMinutes();
            return new Detail(card, content.demoUrl(), content.repoUrl(),
                    content.story(), content.demoWithoutLogin(), WeekSchedule.kst(submission.getUpdatedAt()),
                    Math.round(minutes / 60.0), 0);
        }
    }

    public record Author(@JsonUnwrapped UserSummary user, int streak) {
    }

    // ponytail: demoReachable, hasReadme는 확인 작업을 만들 때 채워요. 그 전엔 null.
    public record Checks(boolean onTime, Boolean demoReachable, Boolean hasReadme, boolean confirmsRule) {
    }

    public static SubmissionDetailResponse of(SubmissionCard card, Submission submission, Week week,
                                              long submissionCount, User author, int streak, Instant now) {
        return new SubmissionDetailResponse(
                Detail.of(card, submission, week),
                WeekSummary.from(week, submissionCount, now),
                new Author(UserSummary.from(author), streak),
                new Checks(!submission.getSubmittedAt().isAfter(week.getSubmissionClosesAt()), null, null, true));
    }
}
