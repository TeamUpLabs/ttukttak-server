package com.ttukttak.domain.subscription.infrastructure;

import com.ttukttak.domain.subscription.domain.Subscription;
import com.ttukttak.domain.subscription.domain.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(name = "email_subscriptions", uniqueConstraints = @UniqueConstraint(columnNames = {"email", "topic"}))
@NoArgsConstructor(access = PROTECTED)
class SubscriptionJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 254) // 소문자로 맞춰서 저장해요
    private String email;

    @Column(nullable = false)
    private String topic;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status;

    @Column(nullable = false, unique = true)
    private String tokenHash;

    @Column(nullable = false)
    private Instant sentAt;

    private Instant confirmedAt;
    private Instant unsubscribedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    static SubscriptionJpaEntity from(Subscription subscription) {
        SubscriptionJpaEntity entity = new SubscriptionJpaEntity();
        entity.id = subscription.getId();
        entity.email = subscription.getEmail();
        entity.topic = subscription.getTopic();
        entity.status = subscription.getStatus();
        entity.tokenHash = subscription.getTokenHash();
        entity.sentAt = subscription.getSentAt();
        entity.confirmedAt = subscription.getConfirmedAt();
        entity.unsubscribedAt = subscription.getUnsubscribedAt();
        return entity;
    }

    Subscription toDomain() {
        return Subscription.builder()
                .id(id)
                .email(email)
                .topic(topic)
                .status(status)
                .tokenHash(tokenHash)
                .sentAt(sentAt)
                .confirmedAt(confirmedAt)
                .unsubscribedAt(unsubscribedAt)
                .build();
    }
}
