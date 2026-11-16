package com.ttukttak.suggestion.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SuggestThemeRequest(
        @NotBlank(message = "주제를 입력해 주세요.") @Size(max = 30, message = "주제는 30자까지 써요.")
        String title,

        @NotBlank(message = "제약을 입력해 주세요.") @Size(max = 80, message = "제약은 80자까지 써요.")
        String constraint,

        @Size(max = 300, message = "이유는 300자까지 써요.")
        String why
) {
    public SuggestThemeRequest {
        title = title == null ? null : title.strip();
        constraint = constraint == null ? null : constraint.strip();
        why = why == null ? "" : why.strip();
    }
}
