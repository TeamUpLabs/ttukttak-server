package com.ttukttak.subscription.presentation;

import com.ttukttak.domain.subscription.domain.SubscriptionMailer;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SubscriptionControllerTest {
    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    EntityManager em;

    @MockitoBean
    SubscriptionMailer mailer;

    @BeforeEach
    void clear() {
        jdbc.update("delete from email_subscriptions");
    }

    @Test
    void subscribe() throws Exception {
        // Host 헤더를 바꿔도 링크는 설정한 사이트 주소로 나가요.
        mvc.perform(write(post("/api/v1/subscriptions")).header("X-Forwarded-Host", "evil.example")
                        .content("{ \"email\": \" Me@Example.com \", \"topic\": \"theme-announcement\" }"))
                .andExpect(status().isAccepted());
        ArgumentCaptor<String> confirm = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> unsubscribe = ArgumentCaptor.forClass(String.class);
        verify(mailer).sendConfirmation(eq("me@example.com"), confirm.capture(), unsubscribe.capture());
        assertThat(confirm.getValue()).startsWith("http://localhost:5173/api/v1/subscriptions/confirm?token=");
        assertThat(jdbc.queryForObject("select token_hash from email_subscriptions", String.class))
                .isNotEqualTo(token(confirm.getValue())); // 토큰은 해시만 저장해요

        // 방금 보냈으면 같은 주소로 다시 신청해도 메일을 또 보내지 않아요. 응답은 같아요.
        subscribe("{ \"email\": \"me@example.com\" }");
        verify(mailer, times(1)).sendConfirmation(anyString(), anyString(), anyString());

        mvc.perform(get("/api/v1/subscriptions/confirm").param("token", "wrong"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/?subscribed=0"));
        mvc.perform(get("/api/v1/subscriptions/confirm").param("token", token(confirm.getValue())))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/?subscribed=1"));
        assertThat(jdbc.queryForObject("select status from email_subscriptions", String.class)).isEqualTo("ACTIVE");

        mvc.perform(get("/api/v1/subscriptions/unsubscribe").param("token", token(unsubscribe.getValue())))
                .andExpect(redirectedUrl("/?unsubscribed=1"));
        assertThat(jdbc.queryForObject("select status from email_subscriptions", String.class)).isEqualTo("UNSUBSCRIBED");
    }

    @Test
    void resubscribe() throws Exception {
        subscribe("{ \"email\": \"me@example.com\" }");
        jdbc.update("update email_subscriptions set status = 'UNSUBSCRIBED', sent_at = now() - interval '1 hour'");
        em.clear(); // 테스트는 요청들이 한 트랜잭션이라 JDBC로 바꾼 값을 다시 읽게 해요

        // 해지했던 주소는 다시 확인 메일을 받아요.
        subscribe("{ \"email\": \"me@example.com\" }");
        verify(mailer, times(2)).sendConfirmation(eq("me@example.com"), anyString(), anyString());
        assertThat(jdbc.queryForObject("select status from email_subscriptions", String.class)).isEqualTo("PENDING");
    }

    @Test
    void mailFailureLooksTheSame() throws Exception {
        doThrow(new RuntimeException("SMTP down")).when(mailer).sendConfirmation(anyString(), anyString(), anyString());
        subscribe("{ \"email\": \"me@example.com\" }");
    }

    @Test
    void validation() throws Exception {
        mvc.perform(write(post("/api/v1/subscriptions")).content("{ \"email\": \"not-an-email\" }"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.error.fields.email").exists());
        mvc.perform(write(post("/api/v1/subscriptions")).content("{ \"email\": \"me@example.com\", \"topic\": \"spam\" }"))
                .andExpect(jsonPath("$.error.fields.topic").exists());
        verifyNoInteractions(mailer);
    }

    private void subscribe(String body) throws Exception {
        mvc.perform(write(post("/api/v1/subscriptions")).content(body))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("pending"));
    }

    private static String token(String url) {
        return url.substring(url.indexOf("token=") + "token=".length());
    }

    private static MockHttpServletRequestBuilder write(MockHttpServletRequestBuilder request) {
        return request.header("X-Requested-With", "ddukddak").contentType(MediaType.APPLICATION_JSON);
    }
}
