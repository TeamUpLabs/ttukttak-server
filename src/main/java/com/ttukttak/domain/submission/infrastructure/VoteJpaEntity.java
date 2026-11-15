package com.ttukttak.domain.submission.infrastructure;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

import static lombok.AccessLevel.PROTECTED;

// 테이블 모양만 정해요. 넣고 빼는 건 VoteJpaRepository의 쿼리로 해요.
@Entity
@Table(name = "votes", indexes = @Index(columnList = "user_id"))
@IdClass(VoteJpaEntity.Key.class)
@NoArgsConstructor(access = PROTECTED)
class VoteJpaEntity {
    @Id
    private UUID submissionId;

    @Id
    private UUID userId;

    @Column(nullable = false)
    private Instant createdAt;

    record Key(UUID submissionId, UUID userId) implements Serializable {
    }
}
