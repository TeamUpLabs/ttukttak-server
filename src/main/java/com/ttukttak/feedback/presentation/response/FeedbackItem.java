package com.ttukttak.feedback.presentation.response;

import com.ttukttak.auth.presentation.common.UserSummary;
import com.ttukttak.domain.feedback.domain.Feedback;
import com.ttukttak.domain.feedback.domain.FeedbackType;
import com.ttukttak.domain.user.domain.User;
import com.ttukttak.week.presentation.common.WeekSchedule;

import java.time.OffsetDateTime;
import java.util.UUID;

/** 피드백 하나. 답글이 없으면 reply가, 비로그인이면 viewer가 null이에요. */
public record FeedbackItem(UUID id, FeedbackType type, String body, UserSummary author, OffsetDateTime createdAt,
                           long helpfulCount, Reply reply, Viewer viewer) {

    public record Reply(String body, OffsetDateTime createdAt) {
    }

    public record Viewer(boolean helpful, boolean isAuthor) {
    }

    public static FeedbackItem of(Feedback feedback, User author, long helpfulCount, UUID viewerId, boolean helpful) {
        var reply = feedback.getReply();
        return new FeedbackItem(feedback.getId(), feedback.getType(), feedback.getBody(), UserSummary.from(author),
                WeekSchedule.kst(feedback.getCreatedAt()), helpfulCount,
                reply == null ? null : new Reply(reply.body(), WeekSchedule.kst(reply.createdAt())),
                viewerId == null ? null : new Viewer(helpful, feedback.isAuthor(viewerId)));
    }
}
