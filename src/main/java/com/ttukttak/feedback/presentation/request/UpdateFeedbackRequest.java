package com.ttukttak.feedback.presentation.request;

import com.ttukttak.domain.feedback.domain.FeedbackType;
import jakarta.validation.constraints.Size;

/** 보낸 필드만 바뀌어요. */
public record UpdateFeedbackRequest(
        FeedbackType type,

        @Size(min = 10, max = 500, message = "피드백은 10–500자로 써요.")
        String body
) {
    public UpdateFeedbackRequest {
        body = body == null ? null : body.strip();
    }
}
