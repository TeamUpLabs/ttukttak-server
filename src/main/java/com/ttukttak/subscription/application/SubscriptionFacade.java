package com.ttukttak.subscription.application;

import com.ttukttak.domain.subscription.application.command.SubscriptionCommandService;
import com.ttukttak.domain.subscription.domain.SubscriptionMailer;
import com.ttukttak.subscription.presentation.request.SubscribeRequest;
import com.ttukttak.subscription.presentation.response.SubscribeResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.time.Instant;

/** 요청·응답 DTO와 도메인 사이를 이어줘요. 트랜잭션은 각 서비스가 가져요. */
@Slf4j
@Component
public class SubscriptionFacade {
    private final SubscriptionCommandService subscriptionCommandService;
    private final SubscriptionMailer subscriptionMailer;
    // 메일 링크의 앞부분. 요청의 Host 헤더로 만들면 남의 주소로 신청해 가짜 도메인 링크를 보낼 수 있어서 설정으로 고정해요.
    private final String baseUrl;

    public SubscriptionFacade(SubscriptionCommandService subscriptionCommandService,
                              SubscriptionMailer subscriptionMailer,
                              @Value("${app.public-base-url}") String baseUrl) {
        this.subscriptionCommandService = subscriptionCommandService;
        this.subscriptionMailer = subscriptionMailer;
        this.baseUrl = baseUrl;
    }

    /** 메일은 저장이 끝난 뒤 보내요. 무슨 일이 있어도 같은 응답을 줘서 가입 여부가 드러나지 않게 해요. */
    // ponytail: 요청 안에서 바로 보내요. SMTP가 느려지면 비동기 큐로 옮겨요.
    public SubscribeResponse subscribe(SubscribeRequest request) {
        try {
            subscriptionCommandService.subscribe(request.email(), request.topic(), Instant.now()).ifPresent(token ->
                    subscriptionMailer.sendConfirmation(request.email(),
                            baseUrl + "/api/v1/subscriptions/confirm?token=" + token,
                            baseUrl + "/api/v1/subscriptions/unsubscribe?token=" + token));
        } catch (DataIntegrityViolationException e) {
            // 같은 주소로 동시에 신청하면 늦은 쪽이 고유 제약에 걸려요. 먼저 온 쪽이 메일을 보내요.
        } catch (RuntimeException e) {
            log.error("구독 확인 메일을 보내지 못했어요", e);
        }
        return SubscribeResponse.PENDING;
    }

    public boolean confirm(String token) {
        return subscriptionCommandService.confirm(token, Instant.now());
    }

    public boolean unsubscribe(String token) {
        return subscriptionCommandService.unsubscribe(token, Instant.now());
    }
}
