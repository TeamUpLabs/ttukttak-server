package com.ttukttak.feedback.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReplyRequest(
        @NotBlank(message = "답글을 입력해 주세요.") @Size(max = 300, message = "답글은 300자까지 써요.")
        String body
) {
    public ReplyRequest {
        body = body == null ? null : body.strip();
    }
}
