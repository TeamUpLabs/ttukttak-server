package com.ttukttak.profile.presentation;

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

import java.sql.Timestamp;
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
class ProfileControllerTest {
    static final OffsetDateTime NOW = OffsetDateTime.now(ZoneOffset.ofHours(9));

    @Autowired
    MockMvc mvc;

    @Autowired
    UserCommandService userCommandService;

    @Autowired
    JdbcTemplate jdbc;

    User alice;
    User bob;

    // w48·w49 결과 발표, w50 휴식, w51 투표 중, w01 제작 중
    @BeforeEach
    void setUp() throws Exception {
        for (String table : new String[]{"follows", "feedback_helpful", "feedback", "votes", "submissions",
                "participations", "weeks"}) {
            jdbc.update("delete from " + table);
        }
        mvc.perform(write(admin(post("/api/v1/admin/weeks/import"))).content("""
                        { "weeks": [ %s, %s,
                          { "id": "2025-w50", "type": "break", "round": null, "code": "W50", "title": "휴식",
                            "schedule": { "opensAt": "%s" } },
                          %s, %s ] }""".formatted(theme("2025-w48", 1, -34), theme("2025-w49", 2, -27),
                        NOW.minusDays(20), theme("2025-w51", 3, -4), theme("2026-w01", 4, 0))))
                .andExpect(status().isNoContent());
        alice = userCommandService.loginWithGithub(9001L, "alice", null, null);
        bob = userCommandService.loginWithGithub(9002L, "bob", null, null);

        submit(alice, "2025-w48", -34);
        submit(alice, "2025-w49", -27);
        UUID alices = submit(alice, "2025-w51", -4);
        UUID bobs = submit(bob, "2025-w51", -4);
        jdbc.update("insert into votes (submission_id, user_id, created_at) values (?, ?, now())", alices, bob.getId());
        for (int i = 0; i < 10; i++) {
            jdbc.update("""
                    insert into feedback (id, submission_id, user_id, type, body, earns_points, created_at, updated_at)
                    values (gen_random_uuid(), ?, ?, 'KUDO', ?, true, now() - (? * interval '1 minute'), now())""",
                    bobs, alice.getId(), "멋진 작품이에요 " + i, 10 - i);
        }
    }

    @Test
    void profile() throws Exception {
        mvc.perform(get("/api/v1/users/alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.handle").value("alice"))
                .andExpect(jsonPath("$.stats.currentStreak").value(3))
                .andExpect(jsonPath("$.stats.bestStreak").value(3))
                .andExpect(jsonPath("$.stats.submissionsLast52").value(3))
                .andExpect(jsonPath("$.stats.top3Count").value(2))
                .andExpect(jsonPath("$.stats.votesReceived").value(1))
                .andExpect(jsonPath("$.stats.feedbackGiven").value(10))
                .andExpect(jsonPath("$.social.followers").value(0))
                .andExpect(jsonPath("$.viewer").value(nullValue()));

        mvc.perform(get("/api/v1/users/alice/streak"))
                .andExpect(jsonPath("$.weeks.length()").value(5))
                .andExpect(jsonPath("$.weeks[0].weekId").value("2025-w48"))
                .andExpect(jsonPath("$.weeks[0].state").value("top3"))
                .andExpect(jsonPath("$.weeks[2].state").value("break"))
                .andExpect(jsonPath("$.weeks[3].state").value("submitted"))
                .andExpect(jsonPath("$.weeks[4].state").value("ongoing"))
                .andExpect(jsonPath("$.current.length").value(3))
                .andExpect(jsonPath("$.current.fromWeekId").value("2025-w48"));
        mvc.perform(get("/api/v1/users/bob/streak")).andExpect(jsonPath("$.weeks[1].state").value("none"));

        mvc.perform(get("/api/v1/users/alice/badges"))
                .andExpect(jsonPath("$.items.length()").value(3))
                .andExpect(jsonPath("$.items[?(@.id == 'streak-3')].unit").value("WEEKS"))
                .andExpect(jsonPath("$.items[?(@.id == 'podium-1')].kind").value("podium"))
                .andExpect(jsonPath("$.items[?(@.id == 'feedback-10')].value").value(10));

        // 제출작 탭: 최신순, 결과 발표 뒤 3위 안이면 메달
        mvc.perform(get("/api/v1/users/alice/submissions").param("limit", "2"))
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].week.code").value("W"))
                .andExpect(jsonPath("$.items[0].voteCount").value(1))
                .andExpect(jsonPath("$.items[0].medal").value(nullValue()))
                .andExpect(jsonPath("$.items[1].medal.rank").value(1))
                .andExpect(jsonPath("$.items[1].medal.track").value("regular"))
                .andExpect(jsonPath("$.nextCursor").value(not(nullValue())));

        mvc.perform(get("/api/v1/users/alice/feedback").param("limit", "3"))
                .andExpect(jsonPath("$.items.length()").value(3))
                .andExpect(jsonPath("$.items[0].body").value("멋진 작품이에요 9"))
                .andExpect(jsonPath("$.items[0].submission.author.handle").value("bob"))
                .andExpect(jsonPath("$.nextCursor").value(not(nullValue())));

        mvc.perform(get("/api/v1/users/nobody/streak")).andExpect(status().isNotFound());
    }

    @Test
    void follow() throws Exception {
        mvc.perform(write(as(bob, put("/api/v1/users/alice/follow"))))
                .andExpect(jsonPath("$.following").value(true))
                .andExpect(jsonPath("$.followers").value(1));
        mvc.perform(write(as(bob, put("/api/v1/users/alice/follow")))).andExpect(jsonPath("$.followers").value(1));
        mvc.perform(write(as(alice, put("/api/v1/users/alice/follow")))).andExpect(status().isForbidden());
        mvc.perform(write(put("/api/v1/users/alice/follow"))).andExpect(status().isUnauthorized());

        mvc.perform(as(bob, get("/api/v1/users/alice")))
                .andExpect(jsonPath("$.social.followers").value(1))
                .andExpect(jsonPath("$.viewer.following").value(true))
                .andExpect(jsonPath("$.viewer.isSelf").value(false));
        mvc.perform(as(bob, get("/api/v1/users/bob"))).andExpect(jsonPath("$.social.following").value(1));
        mvc.perform(as(alice, get("/api/v1/users/alice"))).andExpect(jsonPath("$.viewer.isSelf").value(true));

        mvc.perform(write(as(bob, delete("/api/v1/users/alice/follow"))))
                .andExpect(jsonPath("$.following").value(false))
                .andExpect(jsonPath("$.followers").value(0));
    }

    /** startDay일 뒤(음수면 전)에서 1시간 전에 열린 주차에 1시간 뒤 제출 */
    private UUID submit(User user, String weekId, int startDay) {
        UUID id = UUID.randomUUID();
        Timestamp at = Timestamp.from(NOW.plusDays(startDay).toInstant());
        jdbc.update("""
                insert into submissions (id, week_id, user_id, track, title, summary, demo_url, repo_url,
                  keycap_tint, keycap_caption, stack, demo_without_login, submitted_at, updated_at)
                values (?, ?, ?, 'REGULAR', ?, '버튼 하나로 모스 부호를 보내요.', 'https://a.dev', 'https://github.com/a/b',
                  'NIGHT', 'M', '{}', false, ?, ?)""", id, weekId, user.getId(), weekId + " 작품", at, at);
        return id;
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
