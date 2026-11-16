package com.ttukttak.domain.suggestion.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

import static lombok.AccessLevel.PRIVATE;

/** 푸터의 "주제 제안하기". 시즌 후반 주제 투표 후보로 써요. */
@Getter
@Builder
@AllArgsConstructor(access = PRIVATE)
public class ThemeSuggestion {
    private UUID id;
    private UUID userId;
    private String title;
    private String constraint;
    private String why;
    private Instant createdAt;

    public static ThemeSuggestion create(UUID userId, String title, String constraint, String why) {
        return ThemeSuggestion.builder().userId(userId).title(title).constraint(constraint).why(why).build();
    }
}
