package com.ttukttak.submission.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record HideSubmissionRequest(
        @NotBlank(message = "숨기는 이유를 입력해 주세요.") @Size(max = 300, message = "이유는 300자까지 써요.")
        String reason
) {
}
