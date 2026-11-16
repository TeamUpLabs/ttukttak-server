package com.ttukttak.domain.subscription.infrastructure;

import com.ttukttak.domain.subscription.domain.Subscription;
import com.ttukttak.domain.subscription.domain.SubscriptionRepository;
import com.ttukttak.domain.subscription.domain.SubscriptionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
class SubscriptionRepositoryImpl implements SubscriptionRepository {
    private final SubscriptionJpaRepository jpaRepository;

    @Override
    public Subscription save(Subscription subscription) {
        return jpaRepository.saveAndFlush(SubscriptionJpaEntity.from(subscription)).toDomain();
    }

    @Override
    public Optional<Subscription> findByEmailAndTopic(String email, String topic) {
        return jpaRepository.findByEmailAndTopic(email, topic).map(SubscriptionJpaEntity::toDomain);
    }

    @Override
    public Optional<Subscription> findByUnsubscribeToken(String unsubscribeToken) {
        return jpaRepository.findByUnsubscribeToken(unsubscribeToken).map(SubscriptionJpaEntity::toDomain);
    }

    @Override
    public List<Subscription> findActive(String topic) {
        return jpaRepository.findByTopicAndStatus(topic, SubscriptionStatus.ACTIVE).stream()
                .map(SubscriptionJpaEntity::toDomain).toList();
    }

    @Override
    public Optional<Subscription> findByTokenHash(String tokenHash) {
        return jpaRepository.findByTokenHash(tokenHash).map(SubscriptionJpaEntity::toDomain);
    }
}
