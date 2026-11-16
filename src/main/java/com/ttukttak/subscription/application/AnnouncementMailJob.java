package com.ttukttak.subscription.application;

import com.ttukttak.domain.subscription.application.command.SubscriptionCommandService;
import com.ttukttak.domain.subscription.domain.Subscription;
import com.ttukttak.domain.subscription.domain.SubscriptionMailer;
import com.ttukttak.domain.week.application.command.WeekCommandService;
import com.ttukttak.domain.week.application.query.WeekQueryService;
import com.ttukttak.domain.week.domain.Week;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

/** 공지한 주차가 공개 시각이 되면 구독자에게 주제 공개 메일을 보내요. */
// ponytail: 1분마다 보고, 구독자에게 차례로 보내요. 구독자가 수천 명이 되면 메일 서비스의 대량 발송 API로 옮겨요.
@Slf4j
@Component
public class AnnouncementMailJob {
    static final String TOPIC = "theme-announcement";

    private final WeekQueryService weekQueryService;
    private final WeekCommandService weekCommandService;
    private final SubscriptionCommandService subscriptionCommandService;
    private final SubscriptionMailer subscriptionMailer;
    private final String baseUrl;

    public AnnouncementMailJob(WeekQueryService weekQueryService, WeekCommandService weekCommandService,
                               SubscriptionCommandService subscriptionCommandService,
                               SubscriptionMailer subscriptionMailer, @Value("${app.public-base-url}") String baseUrl) {
        this.weekQueryService = weekQueryService;
        this.weekCommandService = weekCommandService;
        this.subscriptionCommandService = subscriptionCommandService;
        this.subscriptionMailer = subscriptionMailer;
        this.baseUrl = baseUrl;
    }

    @Scheduled(fixedDelay = 60_000, initialDelay = 60_000)
    public void sendDue() {
        for (Week week : weekQueryService.announceMailDue(Instant.now())) {
            // 먼저 가져간 쪽만 보내요. 서버가 여러 대여도 두 번 나가지 않아요.
            if (!weekCommandService.claimAnnounceMail(week.getId())) continue;
            for (Subscription subscriber : subscriptionCommandService.activeRecipients(TOPIC)) {
                try {
                    subscriptionMailer.send(subscriber.getEmail(), "[뚝딱] 이번 주 주제: " + week.getTitle(), """
                            %s

                            %s

                            지금 바로 시작하기: %s
                            구독 해지: %s
                            """.formatted(week.getTitle(), week.getAnnouncement(), baseUrl,
                            SubscriptionFacade.unsubscribeUrl(baseUrl, subscriber.getUnsubscribeToken())));
                } catch (RuntimeException e) {
                    log.error("주제 공개 메일을 보내지 못했어요: {}", week.getId(), e);
                }
            }
        }
    }
}
