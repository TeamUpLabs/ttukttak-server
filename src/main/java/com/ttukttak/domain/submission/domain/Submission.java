package com.ttukttak.domain.submission.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

import static lombok.AccessLevel.PRIVATE;

@Getter
@Builder
@AllArgsConstructor(access = PRIVATE)
public class Submission {
    private UUID id;
    private String weekId;
    private UUID userId;
    private SubmissionContent content;
    private Instant submittedAt;
    private Instant updatedAt;

    public static Submission create(String weekId, UUID userId, SubmissionContent content) {
        return Submission.builder().weekId(weekId).userId(userId).content(content).build();
    }

    public void update(SubmissionContent patch) {
        content = content.merge(patch);
    }

    public boolean isAuthor(UUID userId) {
        return this.userId.equals(userId);
    }
}
