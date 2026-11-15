package com.ttukttak.domain.feedback.domain;

import java.time.Instant;

/** 제출작을 만든 사람이 다는 답글 하나 */
public record Reply(String body, Instant createdAt) {
}
