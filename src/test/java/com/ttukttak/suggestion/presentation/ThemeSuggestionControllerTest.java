package com.ttukttak.suggestion.presentation;

import com.ttukttak.auth.infrastructure.GithubOAuth2UserService;
import com.ttukttak.domain.user.application.command.UserCommandService;
import com.ttukttak.domain.user.domain.User;
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

import java.util.Map;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ThemeSuggestionControllerTest {
    @Autowired
    MockMvc mvc;

    @Autowired
    UserCommandService userCommandService;

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void suggest() throws Exception {
        jdbc.update("delete from theme_suggestions");
        User member = userCommandService.loginWithGithub(9101L, "member", null, null);
        String body = """
                { "title": "소리만으로 조작하기", "constraint": "입력은 마이크로만", "why": "센서 주제를 더 밀어보고 싶어요" }""";

        mvc.perform(write(post("/api/v1/theme-suggestions")).content(body)).andExpect(status().isUnauthorized());
        mvc.perform(write(as(member, post("/api/v1/theme-suggestions"))).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("소리만으로 조작하기"))
                .andExpect(jsonPath("$.author.handle").value("member"));
        mvc.perform(write(as(member, post("/api/v1/theme-suggestions")))
                        .content("{ \"title\": \"" + "가".repeat(31) + "\", \"constraint\": \" \" }"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.error.fields.title").exists())
                .andExpect(jsonPath("$.error.fields.constraint").exists());

        mvc.perform(as(member, get("/api/v1/admin/theme-suggestions"))).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/admin/theme-suggestions").with(oauth2Login().oauth2User(new DefaultOAuth2User(
                        AuthorityUtils.createAuthorityList("ROLE_ADMIN"), Map.of("id", UUID.randomUUID().toString()), "id"))))
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].constraint").value("입력은 마이크로만"))
                .andExpect(jsonPath("$.items[0].why").value("센서 주제를 더 밀어보고 싶어요"));
    }

    private static MockHttpServletRequestBuilder as(User user, MockHttpServletRequestBuilder request) {
        return request.with(oauth2Login().oauth2User(GithubOAuth2UserService.principal(user)));
    }

    private static MockHttpServletRequestBuilder write(MockHttpServletRequestBuilder request) {
        return request.header("X-Requested-With", "ddukddak").contentType(MediaType.APPLICATION_JSON);
    }
}
