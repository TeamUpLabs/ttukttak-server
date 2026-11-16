package com.ttukttak.domain.suggestion.domain;

import java.util.List;

public interface ThemeSuggestionRepository {
    ThemeSuggestion save(ThemeSuggestion suggestion);

    /** 최신순 */
    List<ThemeSuggestion> findPage(int offset, int limit);
}
