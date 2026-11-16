package com.ttukttak.domain.subscription.domain;

/** 구독자에게 메일 보내기 */
public interface SubscriptionMailer {
    void send(String to, String subject, String text);
}
