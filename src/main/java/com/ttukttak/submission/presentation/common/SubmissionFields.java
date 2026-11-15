package com.ttukttak.submission.presentation.common;

import com.ttukttak.domain.submission.domain.Keycap;
import com.ttukttak.domain.submission.domain.KeycapTint;
import com.ttukttak.domain.submission.domain.Story;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 제출·수정 요청이 같이 쓰는 검사 규칙과 다듬기 */
public final class SubmissionFields {
    // https만, 사용자 정보(@)·localhost·사설 IP·IPv6 주소 금지.
    // ponytail: 주소 글자만 봐요. 서버가 데모에 접속하게 되면 DNS로 푼 IP도 다시 확인해요.
    public static final String DEMO_URL = "^https://(?![^/?#]*@)"
            + "(?!(localhost\\.?(?![^:/?#])|127\\.|10\\.|0\\.|169\\.254\\.|192\\.168\\.|172\\.(1[6-9]|2\\d|3[01])\\.|\\[))"
            + "[^\\s/?#:]+(:\\d+)?([/?#]\\S*)?$";
    public static final String DEMO_URL_MESSAGE = "https://로 시작하는 공개 주소여야 해요.";
    public static final String REPO_URL = "^https://github\\.com/[A-Za-z0-9-]+/[A-Za-z0-9._-]+/?$";
    public static final String REPO_URL_MESSAGE = "https://github.com/{owner}/{repo} 형태여야 해요.";

    private SubmissionFields() {
    }

    // ponytail: 이미지는 업로드 API가 생기면 받아요.
    public record Thumbnail(
            @NotNull(message = "썸네일을 골라 주세요.")
            @Pattern(regexp = "keycap", message = "이미지 썸네일은 아직 못 올려요. 키캡을 골라 주세요.")
            String kind,
            String url,
            @NotNull(message = "키캡 색을 골라 주세요.") KeycapTint tint,
            @NotBlank(message = "키캡 글자를 입력해 주세요.") @Size(max = 12, message = "키캡 글자는 12자까지 써요.")
            String caption
    ) {
        public Keycap toDomain() {
            return new Keycap(tint, caption);
        }
    }

    public record StoryInput(
            @Size(max = 400, message = "400자까지 써요.") String what,
            @Size(max = 400, message = "400자까지 써요.") String how,
            @Size(max = 400, message = "400자까지 써요.") String retro
    ) {
        /** 빈 칸은 null로 */
        public Story toDomain() {
            return new Story(blankToNull(what), blankToNull(how), blankToNull(retro));
        }
    }

    public static String strip(String value) {
        return value == null ? null : value.strip();
    }

    /** 앞뒤 공백을 지우고, 대소문자만 다른 건 처음 것 하나만 남겨요. */
    public static List<String> stack(List<String> stack) {
        if (stack == null) return null;
        Map<String, String> unique = new LinkedHashMap<>();
        for (String item : stack) unique.putIfAbsent(item.strip().toLowerCase(), item.strip());
        return List.copyOf(unique.values());
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
