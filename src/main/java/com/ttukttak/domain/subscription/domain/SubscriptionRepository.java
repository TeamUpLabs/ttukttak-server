package com.ttukttak.domain.subscription.domain;

import java.util.Optional;

public interface SubscriptionRepository {
    Subscription save(Subscription subscription);

    Optional<Subscription> findByEmailAndTopic(String email, String topic);

    Optional<Subscription> findByTokenHash(String tokenHash);
}
