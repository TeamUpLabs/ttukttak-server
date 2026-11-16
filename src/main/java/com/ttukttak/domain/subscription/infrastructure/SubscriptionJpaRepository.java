package com.ttukttak.domain.subscription.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface SubscriptionJpaRepository extends JpaRepository<SubscriptionJpaEntity, UUID> {
    Optional<SubscriptionJpaEntity> findByEmailAndTopic(String email, String topic);

    Optional<SubscriptionJpaEntity> findByTokenHash(String tokenHash);
}
