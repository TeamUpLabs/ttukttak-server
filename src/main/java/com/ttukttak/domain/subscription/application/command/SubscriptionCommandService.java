package com.ttukttak.domain.subscription.application.command;

import com.ttukttak.domain.subscription.domain.Subscription;
import com.ttukttak.domain.subscription.domain.SubscriptionRepository;
import com.ttukttak.domain.subscription.domain.Tokens;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

// ponytail: 신청 요청 제한은 아직 없어요. 같은 주소는 10분에 한 번만 보내서 메일 폭탄만 막아요.
@Service
@RequiredArgsConstructor
@Transactional
public class SubscriptionCommandService {
    private final SubscriptionRepository subscriptionRepository;

    /** 확인 메일에 넣을 링크 토큰들 */
    public record Links(String confirmToken, String unsubscribeToken) {
    }

    /** 이미 구독 중이거나 방금 보냈으면 비어 있어요. */
    public Optional<Links> subscribe(String email, String topic, Instant now) {
        String token = Tokens.newToken();
        Optional<Subscription> existing = subscriptionRepository.findByEmailAndTopic(email, topic);
        if (existing.isEmpty()) {
            Subscription saved = subscriptionRepository.save(Subscription.create(email, topic, Tokens.hash(token), now));
            return Optional.of(new Links(token, saved.getUnsubscribeToken()));
        }
        Subscription subscription = existing.get();
        if (!subscription.canResend(now)) return Optional.empty();
        subscription.resend(Tokens.hash(token), now);
        subscriptionRepository.save(subscription);
        return Optional.of(new Links(token, subscription.getUnsubscribeToken()));
    }

    /** 맞는 토큰이면 true */
    public boolean confirm(String token, Instant now) {
        return blank(token) ? false : subscriptionRepository.findByTokenHash(Tokens.hash(token)).map(subscription -> {
            subscription.confirm(now);
            subscriptionRepository.save(subscription);
            return true;
        }).orElse(false);
    }

    public boolean unsubscribe(String token, Instant now) {
        return blank(token) ? false : subscriptionRepository.findByUnsubscribeToken(token).map(subscription -> {
            subscription.unsubscribe(now);
            subscriptionRepository.save(subscription);
            return true;
        }).orElse(false);
    }

    /** 메일을 받을 구독자. 해지 토큰이 없던 주소는 여기서 만들어 줘요. */
    public List<Subscription> activeRecipients(String topic) {
        List<Subscription> recipients = subscriptionRepository.findActive(topic);
        recipients.stream().filter(Subscription::ensureUnsubscribeToken).forEach(subscriptionRepository::save);
        return recipients;
    }

    private static boolean blank(String token) {
        return token == null || token.isBlank();
    }
}
