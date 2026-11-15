package com.ttukttak.submission.presentation.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ttukttak.auth.presentation.common.UserSummary;
import com.ttukttak.domain.submission.domain.KeycapTint;
import com.ttukttak.domain.submission.domain.Scoreboard;
import com.ttukttak.domain.submission.domain.Submission;
import com.ttukttak.domain.submission.domain.SubmissionContent;
import com.ttukttak.domain.submission.domain.Track;
import com.ttukttak.domain.user.domain.User;
import com.ttukttak.week.presentation.common.WeekSchedule;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/** 명세의 SubmissionCard. 비로그인이면 viewer가 null, 투표가 끝나기 전엔 rank 값이 null이에요. */
public record SubmissionCard(UUID id, String weekId, UserSummary author, Track track, String title, String summary,
                             Thumbnail thumbnail, List<String> stack, long voteCount, Rank rank,
                             OffsetDateTime submittedAt, Viewer viewer) {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Thumbnail(String kind, String url, KeycapTint tint, String caption) {
    }

    public record Rank(Integer track, Integer overall) {
    }

    public record Viewer(boolean voted, boolean isAuthor) {
    }

    public static SubmissionCard of(Submission submission, User author, Scoreboard.Score score, boolean showRank,
                                    UUID viewerId, boolean voted) {
        SubmissionContent content = submission.getContent();
        return new SubmissionCard(submission.getId(), submission.getWeekId(), UserSummary.from(author),
                content.track(), content.title(), content.summary(),
                new Thumbnail("keycap", null, content.keycap().tint(), content.keycap().caption()),
                content.stack(), score.votes(),
                showRank ? new Rank(score.trackRank(), score.overallRank()) : new Rank(null, null),
                WeekSchedule.kst(submission.getSubmittedAt()),
                viewerId == null ? null : new Viewer(voted, submission.isAuthor(viewerId)));
    }
}
