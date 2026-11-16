package com.ttukttak.profile.presentation.response;

import com.ttukttak.auth.presentation.common.UserSummary;
import com.ttukttak.domain.feedback.domain.FeedbackType;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/** 프로필의 피드백 활동 탭. 최신순 */
public record ProfileFeedbackListResponse(List<Item> items, String nextCursor) {

    public record Item(UUID id, FeedbackType type, String body, long helpfulCount, OffsetDateTime createdAt,
                       SubmissionRef submission) {
    }

    public record SubmissionRef(UUID id, String title, UserSummary author) {
    }
}
