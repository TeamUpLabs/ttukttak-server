package com.ttukttak.domain.subscription.domain;

/** 구독 확인 메일 보내기 */
public interface SubscriptionMailer {
    void sendConfirmation(String email, String confirmUrl, String unsubscribeUrl);
}
