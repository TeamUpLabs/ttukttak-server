package com.ttukttak.domain.submission.infrastructure;

import com.ttukttak.domain.submission.domain.*;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static lombok.AccessLevel.PROTECTED;

// ponytail: 데모 확인 결과 칸은 그 기능을 만들 때 붙여요.
// 운영자가 숨긴 건 JPA로 읽을 때 빠져요. 네이티브 쿼리는 hidden_at is null을 직접 걸어요.
@Entity
@SQLRestriction("hidden_at is null")
@Table(name = "submissions", uniqueConstraints = @UniqueConstraint(columnNames = {"week_id", "user_id"}))
@NoArgsConstructor(access = PROTECTED)
class SubmissionJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String weekId;

    @Column(nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Track track;

    @Column(nullable = false, length = 30)
    private String title;

    @Column(nullable = false, length = 80)
    private String summary;

    @Column(nullable = false, length = 300)
    private String demoUrl;

    @Column(nullable = false, columnDefinition = "text")
    private String repoUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private KeycapTint keycapTint;

    @Column(nullable = false, length = 12)
    private String keycapCaption;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(nullable = false, columnDefinition = "text[]")
    private List<String> stack;

    @Column(length = 400)
    private String storyWhat;

    @Column(length = 400)
    private String storyHow;

    @Column(length = 400)
    private String storyRetro;

    private boolean demoWithoutLogin;

    // 숨기는 건 SubmissionJpaRepository.hide만 해요.
    @Column(insertable = false, updatable = false)
    private Instant hiddenAt;

    @Column(insertable = false, updatable = false, length = 300)
    private String hiddenReason;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant submittedAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    static SubmissionJpaEntity from(Submission submission) {
        SubmissionContent content = submission.getContent();
        SubmissionJpaEntity entity = new SubmissionJpaEntity();
        entity.id = submission.getId();
        entity.weekId = submission.getWeekId();
        entity.userId = submission.getUserId();
        entity.track = content.track();
        entity.title = content.title();
        entity.summary = content.summary();
        entity.demoUrl = content.demoUrl();
        entity.repoUrl = content.repoUrl();
        entity.keycapTint = content.keycap().tint();
        entity.keycapCaption = content.keycap().caption();
        entity.stack = content.stack();
        Story story = content.story();
        entity.storyWhat = story == null ? null : story.what();
        entity.storyHow = story == null ? null : story.how();
        entity.storyRetro = story == null ? null : story.retro();
        entity.demoWithoutLogin = content.demoWithoutLogin();
        entity.submittedAt = submission.getSubmittedAt();
        entity.updatedAt = submission.getUpdatedAt();
        return entity;
    }

    Submission toDomain() {
        boolean hasStory = storyWhat != null || storyHow != null || storyRetro != null;
        return Submission.builder()
                .id(id)
                .weekId(weekId)
                .userId(userId)
                .content(new SubmissionContent(track, title, summary, demoUrl, repoUrl,
                        new Keycap(keycapTint, keycapCaption), stack,
                        hasStory ? new Story(storyWhat, storyHow, storyRetro) : null, demoWithoutLogin))
                .submittedAt(submittedAt)
                .updatedAt(updatedAt)
                .build();
    }
}
