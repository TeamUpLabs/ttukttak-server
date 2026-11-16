package com.ttukttak.domain.suggestion.application.command;

import com.ttukttak.domain.suggestion.domain.ThemeSuggestion;
import com.ttukttak.domain.suggestion.domain.ThemeSuggestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ThemeSuggestionCommandService {
    private final ThemeSuggestionRepository themeSuggestionRepository;

    public ThemeSuggestion suggest(UUID userId, String title, String constraint, String why) {
        return themeSuggestionRepository.save(ThemeSuggestion.create(userId, title, constraint, why));
    }
}
