package com.ttukttak.domain.subscription.infrastructure;

import com.ttukttak.domain.subscription.domain.SubscriptionMailer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/** SMTP 설정(SPRING_MAIL_HOST 등)이 없으면 보내지 않고 경고만 남겨요. 링크는 토큰이라 로그에 남기지 않아요. */
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
    public void send(String to, String subject, String text) {
        JavaMailSender mail = sender.getIfAvailable();
        if (mail == null) {
            log.warn("SMTP 설정이 없어 메일을 보내지 못했어요.");
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mail.send(message);
    }
}
