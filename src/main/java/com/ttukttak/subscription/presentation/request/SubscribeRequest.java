package com.ttukttak.subscription.presentation.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** topic은 지금 theme-announcement 하나예요. 빠지면 그걸로 해요. */
public record SubscribeRequest(
        @NotBlank(message = "메일 주소를 입력해 주세요.") @Size(max = 254, message = "메일 주소가 너무 길어요.")
        @Email(message = "메일 주소 형식이 아니에요.")
        String email,

        @Pattern(regexp = "theme-announcement", message = "알 수 없는 알림이에요.")
        String topic
) {
    public SubscribeRequest {
        email = email == null ? null : email.strip().toLowerCase();
        topic = topic == null ? "theme-announcement" : topic;
    }
}
