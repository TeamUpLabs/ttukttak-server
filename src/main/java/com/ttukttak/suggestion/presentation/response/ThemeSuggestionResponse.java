package com.ttukttak.suggestion.presentation.response;

import com.ttukttak.auth.presentation.common.UserSummary;
import com.ttukttak.domain.suggestion.domain.ThemeSuggestion;
import com.ttukttak.domain.user.domain.User;
import com.ttukttak.week.presentation.common.WeekSchedule;

import java.time.OffsetDateTime;
import java.util.UUID;

/** 탈퇴한 사람의 제안은 author가 null이에요. */
public record ThemeSuggestionResponse(UUID id, String title, String constraint, String why, UserSummary author,
                                      OffsetDateTime createdAt) {

    public static ThemeSuggestionResponse of(ThemeSuggestion suggestion, User author) {
        return new ThemeSuggestionResponse(suggestion.getId(), suggestion.getTitle(), suggestion.getConstraint(),
                suggestion.getWhy(), author == null ? null : UserSummary.from(author),
                WeekSchedule.kst(suggestion.getCreatedAt()));
    }
}
