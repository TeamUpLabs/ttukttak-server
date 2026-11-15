package com.ttukttak.feedback.presentation.request;

import com.ttukttak.domain.feedback.domain.FeedbackType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateFeedbackRequest(
        @NotNull(message = "피드백 종류를 골라 주세요.") FeedbackType type,

        @NotNull(message = "내용을 입력해 주세요.") @Size(min = 10, max = 500, message = "피드백은 10–500자로 써요.")
        String body
) {
    public CreateFeedbackRequest {
        body = body == null ? null : body.strip();
    }
}
