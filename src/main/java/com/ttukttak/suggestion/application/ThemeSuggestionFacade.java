package com.ttukttak.suggestion.application;

import com.ttukttak.common.OffsetCursor;
import com.ttukttak.domain.suggestion.application.command.ThemeSuggestionCommandService;
import com.ttukttak.domain.suggestion.application.query.ThemeSuggestionQueryService;
import com.ttukttak.domain.suggestion.domain.ThemeSuggestion;
import com.ttukttak.domain.user.application.query.UserQueryService;
import com.ttukttak.domain.user.domain.User;
import com.ttukttak.suggestion.presentation.request.SuggestThemeRequest;
import com.ttukttak.suggestion.presentation.response.ThemeSuggestionListResponse;
import com.ttukttak.suggestion.presentation.response.ThemeSuggestionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/** 요청·응답 DTO와 도메인 사이를 이어줘요. 트랜잭션은 각 서비스가 가져요. */
@Component
@RequiredArgsConstructor
public class ThemeSuggestionFacade {
    private static final int MAX_LIMIT = 50;

    private final ThemeSuggestionCommandService themeSuggestionCommandService;
    private final ThemeSuggestionQueryService themeSuggestionQueryService;
    private final UserQueryService userQueryService;

    public ThemeSuggestionResponse suggest(UUID userId, SuggestThemeRequest request) {
        ThemeSuggestion saved = themeSuggestionCommandService.suggest(userId, request.title(), request.constraint(),
                request.why());
        return ThemeSuggestionResponse.of(saved, userQueryService.get(userId));
    }

    public ThemeSuggestionListResponse list(String cursor, int limit) {
        OffsetCursor at = OffsetCursor.decode(cursor);
        int size = Math.clamp(limit, 1, MAX_LIMIT);
        List<ThemeSuggestion> rows = themeSuggestionQueryService.page(at.offset(), size + 1);
        boolean hasNext = rows.size() > size;
        List<ThemeSuggestion> page = hasNext ? rows.subList(0, size) : rows;
        Map<UUID, User> authors = userQueryService.getAll(page.stream().map(ThemeSuggestion::getUserId).distinct().toList())
                .stream().collect(Collectors.toMap(User::getId, Function.identity()));
        return new ThemeSuggestionListResponse(
                page.stream().map(s -> ThemeSuggestionResponse.of(s, authors.get(s.getUserId()))).toList(),
                hasNext ? at.next(size).encode() : null);
    }
}
