package com.ttukttak.domain.feedback.infrastructure;

import com.ttukttak.domain.feedback.domain.Feedback;
import com.ttukttak.domain.feedback.domain.FeedbackType;
import com.ttukttak.domain.feedback.domain.Reply;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

import static lombok.AccessLevel.PROTECTED;

// ponytail: 운영자 숨김 칸은 그 기능을 만들 때 붙여요.
@Entity
@Table(name = "feedback", indexes = {@Index(columnList = "submission_id"), @Index(columnList = "user_id")})
@NoArgsConstructor(access = PROTECTED)
class FeedbackJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID submissionId;

    @Column(nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FeedbackType type;

    @Column(nullable = false, length = 500)
    private String body;

    private boolean earnsPoints;

    @Column(length = 300)
    private String replyBody;

    private Instant replyAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    static FeedbackJpaEntity from(Feedback feedback) {
        FeedbackJpaEntity entity = new FeedbackJpaEntity();
        entity.id = feedback.getId();
        entity.submissionId = feedback.getSubmissionId();
        entity.userId = feedback.getUserId();
        entity.type = feedback.getType();
        entity.body = feedback.getBody();
        entity.earnsPoints = feedback.isEarnsPoints();
        Reply reply = feedback.getReply();
        entity.replyBody = reply == null ? null : reply.body();
        entity.replyAt = reply == null ? null : reply.createdAt();
        entity.createdAt = feedback.getCreatedAt();
        entity.updatedAt = feedback.getUpdatedAt();
        return entity;
    }

    Feedback toDomain() {
        return Feedback.builder()
                .id(id)
                .submissionId(submissionId)
                .userId(userId)
                .type(type)
                .body(body)
                .earnsPoints(earnsPoints)
                .reply(replyBody == null ? null : new Reply(replyBody, replyAt))
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}
