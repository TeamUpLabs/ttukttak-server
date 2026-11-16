package com.ttukttak.domain.subscription.application.command;

import com.ttukttak.domain.subscription.domain.Subscription;
import com.ttukttak.domain.subscription.domain.SubscriptionRepository;
import com.ttukttak.domain.subscription.domain.Tokens;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

// ponytail: 신청 요청 제한은 아직 없어요. 같은 주소는 10분에 한 번만 보내서 메일 폭탄만 막아요.
@Service
@RequiredArgsConstructor
@Transactional
public class SubscriptionCommandService {
    private final SubscriptionRepository subscriptionRepository;

    /** 확인 메일에 넣을 토큰을 돌려줘요. 이미 구독 중이거나 방금 보냈으면 비어 있어요. */
    public Optional<String> subscribe(String email, String topic, Instant now) {
        String token = Tokens.newToken();
        Optional<Subscription> existing = subscriptionRepository.findByEmailAndTopic(email, topic);
        if (existing.isEmpty()) {
            subscriptionRepository.save(Subscription.create(email, topic, Tokens.hash(token), now));
            return Optional.of(token);
        }
        Subscription subscription = existing.get();
        if (!subscription.canResend(now)) return Optional.empty();
        subscription.resend(Tokens.hash(token), now);
        subscriptionRepository.save(subscription);
        return Optional.of(token);
    }

    /** 맞는 토큰이면 true */
    public boolean confirm(String token, Instant now) {
        return find(token).map(subscription -> {
            subscription.confirm(now);
            subscriptionRepository.save(subscription);
            return true;
        }).orElse(false);
    }

    public boolean unsubscribe(String token, Instant now) {
        return find(token).map(subscription -> {
            subscription.unsubscribe(now);
            subscriptionRepository.save(subscription);
            return true;
        }).orElse(false);
    }

    private Optional<Subscription> find(String token) {
        return token == null || token.isBlank() ? Optional.empty()
                : subscriptionRepository.findByTokenHash(Tokens.hash(token));
    }
}
