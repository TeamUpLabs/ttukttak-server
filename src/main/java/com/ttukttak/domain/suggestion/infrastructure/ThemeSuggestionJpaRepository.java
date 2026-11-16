package com.ttukttak.domain.suggestion.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

interface ThemeSuggestionJpaRepository extends JpaRepository<ThemeSuggestionJpaEntity, UUID> {
    @Query(value = "select * from theme_suggestions order by created_at desc, id offset :offset limit :limit",
            nativeQuery = true)
    List<ThemeSuggestionJpaEntity> findPage(int offset, int limit);
}
