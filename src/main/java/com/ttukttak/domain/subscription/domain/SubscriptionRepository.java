package com.ttukttak.domain.subscription.domain;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository {
    Subscription save(Subscription subscription);

    Optional<Subscription> findByEmailAndTopic(String email, String topic);

    Optional<Subscription> findByTokenHash(String tokenHash);

    Optional<Subscription> findByUnsubscribeToken(String unsubscribeToken);

    List<Subscription> findActive(String topic);
}
