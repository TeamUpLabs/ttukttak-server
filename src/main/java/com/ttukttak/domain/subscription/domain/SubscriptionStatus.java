package com.ttukttak.domain.subscription.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SubscriptionStatus {
    PENDING("확인 대기"),
    ACTIVE("구독 중"),
    UNSUBSCRIBED("해지");

    private final String description;
}
