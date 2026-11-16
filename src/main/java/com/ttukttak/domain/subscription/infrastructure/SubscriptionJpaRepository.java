package com.ttukttak.domain.subscription.infrastructure;

import com.ttukttak.domain.subscription.domain.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface SubscriptionJpaRepository extends JpaRepository<SubscriptionJpaEntity, UUID> {
    Optional<SubscriptionJpaEntity> findByEmailAndTopic(String email, String topic);

    Optional<SubscriptionJpaEntity> findByTokenHash(String tokenHash);

    Optional<SubscriptionJpaEntity> findByUnsubscribeToken(String unsubscribeToken);

    List<SubscriptionJpaEntity> findByTopicAndStatus(String topic, SubscriptionStatus status);
}
