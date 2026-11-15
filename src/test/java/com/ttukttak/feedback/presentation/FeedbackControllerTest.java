package com.ttukttak.feedback.presentation;

import com.jayway.jsonpath.JsonPath;
import com.ttukttak.auth.infrastructure.GithubOAuth2UserService;
import com.ttukttak.domain.user.application.command.UserCommandService;
import com.ttukttak.domain.user.domain.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FeedbackControllerTest {
    static final OffsetDateTime NOW = OffsetDateTime.now(ZoneOffset.ofHours(9));

    @Autowired
    MockMvc mvc;

    @Autowired
    UserCommandService userCommandService;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    EntityManager em;

    User alice; // 제출작 주인
    User bob;
    User carol;
    String submission;

    @BeforeEach
    void setUp() throws Exception {
        jdbc.update("delete from feedback_helpful");
        jdbc.update("delete from feedback");
        jdbc.update("delete from votes");
        jdbc.update("delete from submissions");
        jdbc.update("delete from participations");
        jdbc.update("delete from weeks");
        reschedule(0);
        alice = userCommandService.loginWithGithub(8001L, "alice", null, null);
        bob = userCommandService.loginWithGithub(8002L, "bob", null, null);
        carol = userCommandService.loginWithGithub(8003L, "carol", null, null);
        String created = mvc.perform(write(as(alice, post("/api/v1/weeks/2026-w01/submissions"))).content("""
                        { "track": "regular", "title": "모스", "summary": "버튼 하나로 모스 부호를 보내요.",
                          "demoUrl": "https://morse.example.dev", "repoUrl": "https://github.com/alice/morse",
                          "thumbnail": { "kind": "keycap", "tint": "night", "caption": "M" }, "confirmsRule": true }"""))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        submission = JsonPath.read(created, "$.submission.id");
    }

    @Test
    void feedback() throws Exception {
        mvc.perform(write(as(alice, post(feedbackUrl()))).content(body("ux", "내 거에 남기면 안 돼요")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("own_submission"));
        mvc.perform(write(as(bob, post(feedbackUrl()))).content(body("ux", "짧아")))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.error.fields.body").exists());

        String bobs = id(mvc.perform(write(as(bob, post(feedbackUrl()))).content(body("ux", "길게 누르는 기준이 조금 짧아요")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.feedback.author.handle").value("bob"))
                .andExpect(jsonPath("$.feedback.viewer.isAuthor").value(true))
                .andExpect(jsonPath("$.pointsEarned").value(10))
                .andExpect(jsonPath("$.points").value(10)));
        String carols = id(mvc.perform(write(as(carol, post(feedbackUrl()))).content(body("bug", "새로고침하면 메시지가 사라져요")))
                .andExpect(status().isCreated()));

        // 도움돼요: 여러 번 눌러도 한 번, 내 피드백엔 못 눌러요.
        mvc.perform(write(as(carol, put("/api/v1/feedback/" + bobs + "/helpful"))))
                .andExpect(jsonPath("$.helpful").value(true))
                .andExpect(jsonPath("$.helpfulCount").value(1));
        mvc.perform(write(as(carol, put("/api/v1/feedback/" + bobs + "/helpful"))))
                .andExpect(jsonPath("$.helpfulCount").value(1));
        mvc.perform(write(as(bob, put("/api/v1/feedback/" + bobs + "/helpful")))).andExpect(status().isForbidden());
        mvc.perform(as(bob, get("/api/v1/me"))).andExpect(jsonPath("$.points").value(12));

        // 목록: 기본은 도움돼요 많은 순
        mvc.perform(get(feedbackUrl()))
                .andExpect(jsonPath("$.items[0].id").value(bobs))
                .andExpect(jsonPath("$.items[0].helpfulCount").value(1))
                .andExpect(jsonPath("$.items[0].viewer").value(nullValue()))
                .andExpect(jsonPath("$.counts.all").value(2))
                .andExpect(jsonPath("$.counts.bug").value(1))
                .andExpect(jsonPath("$.counts.kudo").value(0))
                .andExpect(jsonPath("$.nextCursor").value(nullValue()));
        mvc.perform(as(carol, get(feedbackUrl()).param("sort", "recent")))
                .andExpect(jsonPath("$.items[0].id").value(carols))
                .andExpect(jsonPath("$.items[1].viewer.helpful").value(true));
        mvc.perform(get(feedbackUrl()).param("type", "bug"))
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.counts.all").value(2));
        mvc.perform(get("/api/v1/submissions/" + submission)).andExpect(jsonPath("$.submission.feedbackCount").value(2));

        // 수정: 본인만, 24시간 안에만
        mvc.perform(write(as(carol, patch("/api/v1/feedback/" + bobs))).content("{ \"type\": \"kudo\" }"))
                .andExpect(status().isForbidden());
        mvc.perform(write(as(bob, patch("/api/v1/feedback/" + bobs))).content("{ \"type\": \"kudo\" }"))
                .andExpect(jsonPath("$.feedback.type").value("kudo"))
                .andExpect(jsonPath("$.feedback.body").value("길게 누르는 기준이 조금 짧아요"));
        jdbc.update("update feedback set created_at = now() - interval '25 hours' where id = ?", UUID.fromString(bobs));
        em.clear();
        mvc.perform(write(as(bob, patch("/api/v1/feedback/" + bobs))).content("{ \"type\": \"ux\" }"))
                .andExpect(status().isForbidden());

        // 답글: 제출작 주인만, 덮어쓰기
        mvc.perform(write(as(bob, put("/api/v1/feedback/" + carols + "/reply"))).content("{ \"body\": \"제가 답해요\" }"))
                .andExpect(status().isForbidden());
        mvc.perform(write(as(alice, put("/api/v1/feedback/" + carols + "/reply"))).content("{ \"body\": \"고칠게요!\" }"))
                .andExpect(jsonPath("$.feedback.reply.body").value("고칠게요!"));
        mvc.perform(write(as(alice, delete("/api/v1/feedback/" + carols + "/reply"))))
                .andExpect(jsonPath("$.feedback.reply").value(nullValue()));

        // 삭제하면 받은 포인트도 사라져요.
        mvc.perform(write(as(carol, delete("/api/v1/feedback/" + bobs)))).andExpect(status().isForbidden());
        mvc.perform(write(as(bob, delete("/api/v1/feedback/" + bobs)))).andExpect(status().isNoContent());
        mvc.perform(as(bob, get("/api/v1/me"))).andExpect(jsonPath("$.points").value(0));

        // 제출작을 철회하면 피드백도 사라져요.
        mvc.perform(write(as(alice, delete("/api/v1/submissions/" + submission)))).andExpect(status().isNoContent());
        mvc.perform(get(feedbackUrl())).andExpect(status().isNotFound());
        mvc.perform(write(as(alice, put("/api/v1/feedback/" + carols + "/reply"))).content("{ \"body\": \"x\" }"))
                .andExpect(status().isNotFound());
    }

    @Test
    void points() throws Exception {
        // 한 주 최대 100점
        for (int i = 0; i < 10; i++) {
            mvc.perform(write(as(bob, post(feedbackUrl()))).content(body("kudo", "멋져요 정말 멋져요 " + i)))
                    .andExpect(jsonPath("$.pointsEarned").value(10));
        }
        mvc.perform(write(as(bob, post(feedbackUrl()))).content(body("kudo", "열한 번째 칭찬이에요")))
                .andExpect(jsonPath("$.pointsEarned").value(0))
                .andExpect(jsonPath("$.points").value(100));

        // 결과 발표 뒤에 남긴 건 포인트가 없어요.
        reschedule(-8);
        mvc.perform(write(as(carol, post(feedbackUrl()))).content(body("kudo", "늦게 남기는 칭찬이에요")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pointsEarned").value(0));
    }

    private String feedbackUrl() {
        return "/api/v1/submissions/" + submission + "/feedback";
    }

    private static String id(org.springframework.test.web.servlet.ResultActions result) throws Exception {
        return JsonPath.read(result.andReturn().getResponse().getContentAsString(), "$.feedback.id");
    }

    private static String body(String type, String body) {
        return "{ \"type\": \"%s\", \"body\": \"%s\" }".formatted(type, body);
    }

    /** 2026-w01을 startDay일 뒤(음수면 전)에서 1시간 전에 열린 것으로 가져와요. 3일 제작, 3일 투표, 1일 집계. */
    private void reschedule(int startDay) throws Exception {
        OffsetDateTime opens = NOW.plusDays(startDay).minusHours(1);
        mvc.perform(write(admin(post("/api/v1/admin/weeks/import"))).content("""
                        { "weeks": [ { "id": "2026-w01", "type": "theme", "round": 1, "code": "W1", "title": "주제",
                          "schedule": { "opensAt": "%s", "submissionClosesAt": "%s", "votingClosesAt": "%s", "resultsAt": "%s" } } ] }"""
                        .formatted(opens, opens.plusDays(3), opens.plusDays(6), opens.plusDays(7))))
                .andExpect(status().isNoContent());
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
