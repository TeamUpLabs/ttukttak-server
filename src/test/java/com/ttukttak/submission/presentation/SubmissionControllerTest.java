package com.ttukttak.submission.presentation;

import com.jayway.jsonpath.JsonPath;
import com.ttukttak.auth.infrastructure.GithubOAuth2UserService;
import com.ttukttak.domain.user.application.command.UserCommandService;
import com.ttukttak.domain.user.domain.User;
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

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SubmissionControllerTest {
    static final OffsetDateTime NOW = OffsetDateTime.now(ZoneOffset.ofHours(9));

    @Autowired
    MockMvc mvc;

    @Autowired
    UserCommandService userCommandService;

    @Autowired
    JdbcTemplate jdbc;

    User alice;
    User bob;

    @BeforeEach
    void setUp() throws Exception {
        jdbc.update("delete from submissions");
        jdbc.update("delete from participations");
        jdbc.update("delete from weeks");
        mvc.perform(write(admin(post("/api/v1/admin/weeks/import"))).content("""
                        { "weeks": [ %s, %s ] }""".formatted(theme("2025-w51", 1, -20), theme("2026-w01", 2, 0))))
                .andExpect(status().isNoContent());
        alice = userCommandService.loginWithGithub(7001L, "alice", null, null);
        bob = userCommandService.loginWithGithub(7002L, "bob", null, null);
    }

    @Test
    void submit() throws Exception {
        String created = mvc.perform(write(as(alice, post("/api/v1/weeks/2026-w01/submissions"))).content(body("regular", "모스 메신저")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.submission.title").value("모스 메신저"))
                .andExpect(jsonPath("$.submission.author.handle").value("alice"))
                .andExpect(jsonPath("$.submission.thumbnail.kind").value("keycap"))
                .andExpect(jsonPath("$.submission.thumbnail.tint").value("night"))
                .andExpect(jsonPath("$.submission.stack.length()").value(2)) // 대소문자만 다른 건 하나로
                .andExpect(jsonPath("$.submission.story.how").value(nullValue()))
                .andExpect(jsonPath("$.submission.viewer.isAuthor").value(true))
                .andExpect(jsonPath("$.streak.current").value(1))
                .andExpect(jsonPath("$.streak.isNewBest").value(true))
                .andReturn().getResponse().getContentAsString();
        String id = JsonPath.read(created, "$.submission.id");

        // 제출하면 참가로 쳐요. 참가 취소는 안 돼요.
        mvc.perform(as(alice, get("/api/v1/me")))
                .andExpect(jsonPath("$.currentWeek.joined").value(true))
                .andExpect(jsonPath("$.currentWeek.submissionId").value(id))
                .andExpect(jsonPath("$.currentWeek.streak").value(1));
        mvc.perform(get("/api/v1/users/alice"))
                .andExpect(jsonPath("$.stats.currentStreak").value(1))
                .andExpect(jsonPath("$.stats.bestStreak").value(1));
        mvc.perform(write(as(alice, delete("/api/v1/weeks/current/participation"))))
                .andExpect(status().isConflict());

        // 한 주 한 번, 마감 지난 주는 안 돼요.
        mvc.perform(write(as(alice, post("/api/v1/weeks/2026-w01/submissions"))).content(body("regular", "또")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("already_submitted"))
                .andExpect(jsonPath("$.error.submissionId").value(id));
        mvc.perform(write(as(bob, post("/api/v1/weeks/2025-w51/submissions"))).content(body("regular", "늦음")))
                .andExpect(jsonPath("$.error.code").value("submission_closed"));
        mvc.perform(write(post("/api/v1/weeks/2026-w01/submissions")).content(body("regular", "익명")))
                .andExpect(status().isUnauthorized());

        // 상세: 비로그인은 viewer가 null
        mvc.perform(get("/api/v1/submissions/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.submission.id").value(id))
                .andExpect(jsonPath("$.submission.demoUrl").value("https://morse.example.dev"))
                .andExpect(jsonPath("$.submission.workHours").value(1))
                .andExpect(jsonPath("$.submission.viewer").value(nullValue()))
                .andExpect(jsonPath("$.week.submissionCount").value(1))
                .andExpect(jsonPath("$.author.handle").value("alice"))
                .andExpect(jsonPath("$.author.streak").value(1))
                .andExpect(jsonPath("$.checks.onTime").value(true));

        // 수정: 본인만, 보낸 칸만
        mvc.perform(write(as(bob, patch("/api/v1/submissions/" + id))).content("{ \"title\": \"뺏기\" }"))
                .andExpect(status().isForbidden());
        mvc.perform(write(as(alice, patch("/api/v1/submissions/" + id))).content("{ \"title\": \"  모스 2  \" }"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.submission.title").value("모스 2"))
                .andExpect(jsonPath("$.submission.summary").value("버튼 하나로 모스 부호를 보내요."));

        // 철회하면 404
        mvc.perform(write(as(bob, delete("/api/v1/submissions/" + id)))).andExpect(status().isForbidden());
        mvc.perform(write(as(alice, delete("/api/v1/submissions/" + id)))).andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/submissions/" + id)).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/submissions/not-a-uuid")).andExpect(status().isBadRequest());
    }

    @Test
    void list() throws Exception {
        mvc.perform(write(as(alice, post("/api/v1/weeks/2026-w01/submissions"))).content(body("regular", "첫째")))
                .andExpect(status().isCreated());
        mvc.perform(write(as(bob, post("/api/v1/weeks/2026-w01/submissions"))).content(body("expert", "둘째")))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/v1/weeks/current")).andExpect(jsonPath("$.stats.submissions").value(2));

        // 제작 기간 기본은 무작위. 씨앗을 커서로 넘겨서 다음 페이지와 겹치지 않아요.
        String first = mvc.perform(get("/api/v1/weeks/2026-w01/submissions").param("limit", "1"))
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.nextCursor").value(not(nullValue())))
                .andReturn().getResponse().getContentAsString();
        mvc.perform(get("/api/v1/weeks/2026-w01/submissions").param("limit", "1")
                        .param("cursor", JsonPath.<String>read(first, "$.nextCursor")))
                .andExpect(jsonPath("$.items[0].id").value(not(JsonPath.<String>read(first, "$.items[0].id"))))
                .andExpect(jsonPath("$.nextCursor").value(nullValue()));

        mvc.perform(as(alice, get("/api/v1/weeks/2026-w01/submissions").param("sort", "recent")))
                .andExpect(jsonPath("$.items[0].title").value("둘째"))
                .andExpect(jsonPath("$.items[0].viewer.isAuthor").value(false))
                .andExpect(jsonPath("$.items[1].viewer.isAuthor").value(true));
        String bobs = mvc.perform(get("/api/v1/weeks/2026-w01/submissions").param("track", "expert"))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.items[0].title").value("둘째"))
                .andReturn().getResponse().getContentAsString();
        mvc.perform(get("/api/v1/weeks/2026-w01/submissions").param("exclude", JsonPath.<String>read(bobs, "$.items[0].id")))
                .andExpect(jsonPath("$.items[0].title").value("첫째"));

        mvc.perform(get("/api/v1/weeks/2026-w01/submissions").param("track", "pro")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/weeks/2026-w01/submissions").param("cursor", "!!")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/weeks/2099-w01/submissions")).andExpect(status().isNotFound());
    }

    @Test
    void validation() throws Exception {
        mvc.perform(write(as(alice, post("/api/v1/weeks/2026-w01/submissions"))).content("""
                        { "track": "regular", "title": "   ", "summary": "짧아", "demoUrl": "https://localhost:3000",
                          "repoUrl": "https://gitlab.com/a/b", "thumbnail": { "kind": "image", "url": "https://x.dev/a.png" },
                          "stack": ["1","2","3","4","5","6","7"], "confirmsRule": false }"""))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.error.fields.title").exists())
                .andExpect(jsonPath("$.error.fields.summary").exists())
                .andExpect(jsonPath("$.error.fields.demoUrl").exists())
                .andExpect(jsonPath("$.error.fields.repoUrl").exists())
                .andExpect(jsonPath("$.error.fields['thumbnail.kind']").exists())
                .andExpect(jsonPath("$.error.fields.stack").exists())
                .andExpect(jsonPath("$.error.fields.confirmsRule").exists());

        for (String demo : new String[]{"http://a.dev", "https://192.168.0.1", "https://user@a.dev", "https://[::1]/"}) {
            mvc.perform(write(as(alice, post("/api/v1/weeks/2026-w01/submissions")))
                            .content(body("regular", "x").replace("https://morse.example.dev", demo)))
                    .andExpect(jsonPath("$.error.fields.demoUrl").exists());
        }
    }

    private static String body(String track, String title) {
        return """
                { "track": "%s", "title": "%s", "summary": "버튼 하나로 모스 부호를 보내요.",
                  "demoUrl": "https://morse.example.dev", "repoUrl": "https://github.com/alice/morse",
                  "thumbnail": { "kind": "keycap", "tint": "night", "caption": "·—· —·—" },
                  "stack": ["Svelte", "svelte ", "WebSocket"], "story": { "what": "모스", "how": " " },
                  "confirmsRule": true }""".formatted(track, title);
    }

    /** startDay일 뒤(음수면 전)에서 1시간 전에 열리는 주제 주. 3일 제작, 3일 투표, 1일 집계. */
    private static String theme(String id, int round, int startDay) {
        OffsetDateTime opens = NOW.plusDays(startDay).minusHours(1);
        return """
                { "id": "%s", "type": "theme", "round": %d, "code": "W", "title": "%s 주제",
                  "schedule": { "opensAt": "%s", "submissionClosesAt": "%s", "votingClosesAt": "%s", "resultsAt": "%s" } }"""
                .formatted(id, round, id, opens, opens.plusDays(3), opens.plusDays(6), opens.plusDays(7));
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
