package com.ttukttak.admin;

import com.jayway.jsonpath.JsonPath;
import com.ttukttak.auth.infrastructure.GithubOAuth2UserService;
import com.ttukttak.domain.subscription.domain.SubscriptionMailer;
import com.ttukttak.domain.user.application.command.UserCommandService;
import com.ttukttak.domain.user.domain.User;
import com.ttukttak.subscription.application.AnnouncementMailJob;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminControllerTest {
    static final OffsetDateTime NOW = OffsetDateTime.now(ZoneOffset.ofHours(9));

    @Autowired
    MockMvc mvc;

    @Autowired
    UserCommandService userCommandService;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    EntityManager em;

    @Autowired
    AnnouncementMailJob announcementMailJob;

    @MockitoBean
    SubscriptionMailer mailer;

    @BeforeEach
    void setUp() throws Exception {
        for (String table : new String[]{"email_subscriptions", "follows", "feedback_helpful", "feedback", "votes",
                "submissions", "participations", "weeks"}) {
            jdbc.update("delete from " + table);
        }
        mvc.perform(write(admin(post("/api/v1/admin/weeks/import"))).content("""
                        { "weeks": [ %s, %s ] }""".formatted(theme("2026-w01", 1, 0), theme("2026-w02", 2, 7))))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateWeek() throws Exception {
        User member = userCommandService.loginWithGithub(9201L, "member", null, null);
        mvc.perform(write(as(member, patch("/api/v1/admin/weeks/2026-w02"))).content("{ \"title\": \"x\" }"))
                .andExpect(status().isForbidden());

        // 보낸 필드만 바뀌어요.
        mvc.perform(write(admin(patch("/api/v1/admin/weeks/2026-w02"))).content("""
                        { "title": "소리만으로", "constraints": ["입력은 마이크로만"] }"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.week.id").value("2026-w02"))
                .andExpect(jsonPath("$.week.phase").value("upcoming"));
        assertThat(jdbc.queryForObject("select title from weeks where id = '2026-w02'", String.class)).isEqualTo("소리만으로");

        // 공개된 주차의 일정은 force가 있어야 바꿔요. 일정 규칙은 늘 지켜야 해요.
        String later = schedule(1);
        mvc.perform(write(admin(patch("/api/v1/admin/weeks/2026-w01"))).content("{ \"schedule\": " + later + " }"))
                .andExpect(status().isConflict());
        mvc.perform(write(admin(patch("/api/v1/admin/weeks/2026-w01"))).content("{ \"schedule\": " + later + ", \"force\": true }"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.week.phase").value("upcoming"));
        mvc.perform(write(admin(patch("/api/v1/admin/weeks/2026-w02"))).content("""
                        { "schedule": { "opensAt": "%s" } }""".formatted(NOW.plusDays(7))))
                .andExpect(status().isUnprocessableContent());
        mvc.perform(write(admin(patch("/api/v1/admin/weeks/2099-w01"))).content("{}")).andExpect(status().isNotFound());
    }

    @Test
    void announce() throws Exception {
        jdbc.update("""
                insert into email_subscriptions (id, email, topic, status, token_hash, sent_at, created_at)
                values (gen_random_uuid(), 'fan@example.com', 'theme-announcement', 'ACTIVE', 'h1', now(), now()),
                       (gen_random_uuid(), 'wait@example.com', 'theme-announcement', 'PENDING', 'h2', now(), now())""");

        mvc.perform(write(admin(post("/api/v1/admin/weeks/2026-w02/announce")))).andExpect(status().isNoContent());
        mvc.perform(write(admin(post("/api/v1/admin/weeks/2026-w01/announce")))).andExpect(status().isNoContent());
        assertThat(jdbc.queryForObject("select status from weeks where id = '2026-w01'", String.class)).isEqualTo("ANNOUNCED");

        // 공개된 w01만, 구독 중인 주소에만, 한 번만 보내요. w02는 공개 시각을 기다려요.
        announcementMailJob.sendDue();
        announcementMailJob.sendDue();
        ArgumentCaptor<String> text = ArgumentCaptor.forClass(String.class);
        verify(mailer, times(1)).send(eq("fan@example.com"), eq("[뚝딱] 이번 주 주제: 2026-w01 주제"), text.capture());
        verify(mailer, never()).send(eq("wait@example.com"), anyString(), anyString());
        assertThat(jdbc.queryForObject("select announce_mail_pending from weeks where id = '2026-w02'", Boolean.class)).isTrue();

        // 해지 토큰이 없던 주소도 메일의 해지 링크로 해지돼요.
        String unsubscribe = text.getValue().replaceAll("(?s).*/subscriptions/unsubscribe\\?token=(\\S+).*", "$1");
        em.clear();
        mvc.perform(get("/api/v1/subscriptions/unsubscribe").param("token", unsubscribe));
        assertThat(jdbc.queryForObject("select status from email_subscriptions where email = 'fan@example.com'", String.class))
                .isEqualTo("UNSUBSCRIBED");
    }

    @Test
    void hide() throws Exception {
        User alice = userCommandService.loginWithGithub(9202L, "alice", null, null);
        User bob = userCommandService.loginWithGithub(9203L, "bob", null, null);
        String submission = JsonPath.read(mvc.perform(write(as(alice, post("/api/v1/weeks/2026-w01/submissions"))).content("""
                        { "track": "regular", "title": "모스", "summary": "버튼 하나로 모스 부호를 보내요.",
                          "demoUrl": "https://morse.example.dev", "repoUrl": "https://github.com/alice/morse",
                          "thumbnail": { "kind": "keycap", "tint": "night", "caption": "M" }, "confirmsRule": true }"""))
                .andReturn().getResponse().getContentAsString(), "$.submission.id");
        String feedback = JsonPath.read(mvc.perform(write(as(bob, post("/api/v1/submissions/" + submission + "/feedback")))
                        .content("{ \"type\": \"kudo\", \"body\": \"정말 멋진 작품이에요\" }"))
                .andReturn().getResponse().getContentAsString(), "$.feedback.id");

        // 피드백 숨김: 목록과 포인트에서 빠져요.
        mvc.perform(write(admin(delete("/api/v1/admin/feedback/" + feedback)))).andExpect(status().isNoContent());
        mvc.perform(write(admin(delete("/api/v1/admin/feedback/" + feedback)))).andExpect(status().isNotFound());
        em.clear();
        mvc.perform(get("/api/v1/submissions/" + submission + "/feedback")).andExpect(jsonPath("$.counts.all").value(0));
        mvc.perform(as(bob, get("/api/v1/me"))).andExpect(jsonPath("$.points").value(0));

        // 제출작 숨김: 이유가 필요하고, 모든 공개 화면에서 빠져요.
        mvc.perform(write(admin(delete("/api/v1/admin/submissions/" + submission))).content("{}"))
                .andExpect(status().isUnprocessableContent());
        mvc.perform(write(as(bob, delete("/api/v1/admin/submissions/" + submission))).content("{ \"reason\": \"x\" }"))
                .andExpect(status().isForbidden());
        mvc.perform(write(admin(delete("/api/v1/admin/submissions/" + submission))).content("{ \"reason\": \"규칙 위반\" }"))
                .andExpect(status().isNoContent());
        assertThat(jdbc.queryForObject("select hidden_reason from submissions where id = ?::uuid", String.class, submission))
                .isEqualTo("규칙 위반");
        em.clear();
        mvc.perform(get("/api/v1/submissions/" + submission)).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/weeks/2026-w01/submissions")).andExpect(jsonPath("$.total").value(0));
        mvc.perform(get("/api/v1/weeks/current")).andExpect(jsonPath("$.stats.submissions").value(0));
        mvc.perform(get("/api/v1/users/alice/submissions")).andExpect(jsonPath("$.items.length()").value(0));
    }

    /** startDay일 뒤(음수면 전)에서 1시간 전에 열리는 일정. 3일 제작, 3일 투표, 1일 집계. */
    private static String schedule(int startDay) {
        OffsetDateTime opens = NOW.plusDays(startDay).minusHours(1);
        return """
                { "opensAt": "%s", "submissionClosesAt": "%s", "votingClosesAt": "%s", "resultsAt": "%s" }"""
                .formatted(opens, opens.plusDays(3), opens.plusDays(6), opens.plusDays(7));
    }

    private static String theme(String id, int round, int startDay) {
        return """
                { "id": "%s", "type": "theme", "round": %d, "code": "W", "title": "%s 주제", "announcement": "공지",
                  "schedule": %s }""".formatted(id, round, id, schedule(startDay));
    }

    private static MockHttpServletRequestBuilder as(User user, MockHttpServletRequestBuilder request) {
        return request.with(oauth2Login().oauth2User(GithubOAuth2UserService.principal(user)));
    }

    private static MockHttpServletRequestBuilder admin(MockHttpServletRequestBuilder request) {
        return request.with(oauth2Login().oauth2User(new DefaultOAuth2User(
                AuthorityUtils.createAuthorityList("ROLE_ADMIN"), Map.of("id", UUID.randomUUID().toString()), "id")));
    }

    private static MockHttpServletRequestBuilder write(MockHttpServletRequestBuilder request) {
        return request.header("X-Requested-With", "ddukddak").contentType(MediaType.APPLICATION_JSON);
    }
}
