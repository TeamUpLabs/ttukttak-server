package com.ttukttak.domain.suggestion.application.query;

import com.ttukttak.domain.suggestion.domain.ThemeSuggestion;
import com.ttukttak.domain.suggestion.domain.ThemeSuggestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ThemeSuggestionQueryService {
    private final ThemeSuggestionRepository themeSuggestionRepository;

    /** 최신순 */
    public List<ThemeSuggestion> page(int offset, int limit) {
        return themeSuggestionRepository.findPage(offset, limit);
    }
}
