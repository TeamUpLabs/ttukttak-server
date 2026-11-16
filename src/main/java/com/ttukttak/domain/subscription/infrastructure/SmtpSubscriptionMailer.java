package com.ttukttak.domain.subscription.infrastructure;

import com.ttukttak.domain.subscription.domain.SubscriptionMailer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/** SPRING_MAIL_HOST 등 SMTP 설정이 없으면(로컬) 메일 대신 링크를 로그에 남겨요. */
@Slf4j
@Component
class SmtpSubscriptionMailer implements SubscriptionMailer {
    private final ObjectProvider<JavaMailSender> sender;
    private final String from;

    SmtpSubscriptionMailer(ObjectProvider<JavaMailSender> sender, @Value("${app.mail.from}") String from) {
        this.sender = sender;
        this.from = from;
    }

    @Override
    public void sendConfirmation(String email, String confirmUrl, String unsubscribeUrl) {
        JavaMailSender mail = sender.getIfAvailable();
        if (mail == null) {
            log.info("SMTP 설정이 없어 구독 확인 메일 대신 링크를 남겨요: {}", confirmUrl);
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(email);
        message.setSubject("[뚝딱] 주제 공개 알림 신청을 확인해 주세요");
        message.setText("""
                아래 링크를 누르면 매주 월요일 10:00 새 주제가 공개될 때 메일로 알려드려요.

                %s

                신청한 적이 없다면 이 메일은 무시해 주세요.
                구독 해지: %s
                """.formatted(confirmUrl, unsubscribeUrl));
        mail.send(message);
    }
}
