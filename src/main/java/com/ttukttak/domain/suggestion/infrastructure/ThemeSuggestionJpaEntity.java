package com.ttukttak.domain.suggestion.infrastructure;

import com.ttukttak.domain.suggestion.domain.ThemeSuggestion;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(name = "theme_suggestions")
@NoArgsConstructor(access = PROTECTED)
class ThemeSuggestionJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 30)
    private String title;

    @Column(name = "constraint_text", nullable = false, length = 80) // constraint는 SQL 예약어예요
    private String constraint;

    @Column(nullable = false, length = 300)
    private String why;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    static ThemeSuggestionJpaEntity from(ThemeSuggestion suggestion) {
        ThemeSuggestionJpaEntity entity = new ThemeSuggestionJpaEntity();
        entity.id = suggestion.getId();
        entity.userId = suggestion.getUserId();
        entity.title = suggestion.getTitle();
        entity.constraint = suggestion.getConstraint();
        entity.why = suggestion.getWhy();
        entity.createdAt = suggestion.getCreatedAt();
        return entity;
    }

    ThemeSuggestion toDomain() {
        return ThemeSuggestion.builder()
                .id(id)
                .userId(userId)
                .title(title)
                .constraint(constraint)
                .why(why)
                .createdAt(createdAt)
                .build();
    }
}
