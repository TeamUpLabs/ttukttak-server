package com.ttukttak.domain.suggestion.infrastructure;

import com.ttukttak.domain.suggestion.domain.ThemeSuggestion;
import com.ttukttak.domain.suggestion.domain.ThemeSuggestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
class ThemeSuggestionRepositoryImpl implements ThemeSuggestionRepository {
    private final ThemeSuggestionJpaRepository jpaRepository;

    @Override
    public ThemeSuggestion save(ThemeSuggestion suggestion) {
        return jpaRepository.saveAndFlush(ThemeSuggestionJpaEntity.from(suggestion)).toDomain();
    }

    @Override
    public List<ThemeSuggestion> findPage(int offset, int limit) {
        return jpaRepository.findPage(offset, limit).stream().map(ThemeSuggestionJpaEntity::toDomain).toList();
    }
}
