package com.ttukttak.domain.feedback.infrastructure;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

import static lombok.AccessLevel.PROTECTED;

// 테이블 모양만 정해요. 넣고 빼는 건 FeedbackJpaRepository의 쿼리로 해요.
@Entity
@Table(name = "feedback_helpful")
@IdClass(FeedbackHelpfulJpaEntity.Key.class)
@NoArgsConstructor(access = PROTECTED)
class FeedbackHelpfulJpaEntity {
    @Id
    private UUID feedbackId;

    @Id
    private UUID userId;

    @Column(nullable = false)
    private Instant createdAt;

    record Key(UUID feedbackId, UUID userId) implements Serializable {
    }
}
