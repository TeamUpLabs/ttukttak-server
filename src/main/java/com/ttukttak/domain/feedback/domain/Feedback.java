package com.ttukttak.domain.feedback.domain;

import com.ttukttak.domain.feedback.exception.FeedbackForbiddenException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static lombok.AccessLevel.PRIVATE;

@Getter
@Builder
@AllArgsConstructor(access = PRIVATE)
public class Feedback {
    private static final Duration EDITABLE = Duration.ofHours(24);

    private UUID id;
    private UUID submissionId;
    private UUID userId;
    private FeedbackType type;
    private String body;
    private boolean earnsPoints;     // 제작·투표 기간에 남긴 것만 포인트가 쌓여요
    private Reply reply;
    private Instant createdAt;
    private Instant updatedAt;

    public static Feedback create(UUID submissionId, UUID userId, FeedbackType type, String body, boolean earnsPoints) {
        return Feedback.builder().submissionId(submissionId).userId(userId).type(type).body(body)
                .earnsPoints(earnsPoints).build();
    }

    /** 작성 후 24시간 안에만. null은 그대로 둬요. */
    public void edit(FeedbackType type, String body, Instant now) {
        if (now.isAfter(createdAt.plus(EDITABLE))) throw FeedbackForbiddenException.editClosed();
        if (type != null) this.type = type;
        if (body != null) this.body = body;
    }

    /** 답글은 하나라 덮어써요. null이면 지워요. */
    public void reply(String body, Instant now) {
        this.reply = body == null ? null : new Reply(body, now);
    }

    public boolean isAuthor(UUID userId) {
        return this.userId.equals(userId);
    }
}
