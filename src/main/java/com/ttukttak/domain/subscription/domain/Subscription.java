package com.ttukttak.domain.subscription.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static lombok.AccessLevel.PRIVATE;

/** 메일 알림 구독. 확인 메일의 링크를 눌러야 ACTIVE가 돼요(이중 확인). */
@Getter
@Builder
@AllArgsConstructor(access = PRIVATE)
public class Subscription {
    // 남의 주소로 신청을 반복해도 메일이 쏟아지지 않게 해요.
    private static final Duration RESEND_AFTER = Duration.ofMinutes(10);

    private UUID id;
    private String email;
    private String topic;
    private SubscriptionStatus status;
    private String tokenHash;        // 확인 링크 토큰의 해시
    private String unsubscribeToken; // 해지 링크 토큰. 메일마다 넣어야 해서 원래 값을 둬요
    private Instant sentAt;
    private Instant confirmedAt;
    private Instant unsubscribedAt;

    public static Subscription create(String email, String topic, String tokenHash, Instant now) {
        return Subscription.builder().email(email).topic(topic).status(SubscriptionStatus.PENDING)
                .tokenHash(tokenHash).unsubscribeToken(Tokens.newToken()).sentAt(now).build();
    }

    /** 이미 구독 중이거나 방금 보냈으면 다시 안 보내요. */
    public boolean canResend(Instant now) {
        return status != SubscriptionStatus.ACTIVE && !now.isBefore(sentAt.plus(RESEND_AFTER));
    }

    /** 새 토큰으로 확인 메일을 다시 보내요. 해지했던 주소도 다시 확인을 받아요. */
    public void resend(String tokenHash, Instant now) {
        this.status = SubscriptionStatus.PENDING;
        this.tokenHash = tokenHash;
        this.sentAt = now;
        ensureUnsubscribeToken();
    }

    /** 해지 토큰이 생기기 전에 신청한 주소에도 만들어 줘요. 만들었으면 true */
    public boolean ensureUnsubscribeToken() {
        if (unsubscribeToken != null) return false;
        unsubscribeToken = Tokens.newToken();
        return true;
    }

    public void confirm(Instant now) {
        if (status != SubscriptionStatus.PENDING) return;
        this.status = SubscriptionStatus.ACTIVE;
        this.confirmedAt = now;
    }

    public void unsubscribe(Instant now) {
        if (status == SubscriptionStatus.UNSUBSCRIBED) return;
        this.status = SubscriptionStatus.UNSUBSCRIBED;
        this.unsubscribedAt = now;
    }
}
