package com.ttukttak.domain.user.infrastructure;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

import static lombok.AccessLevel.PROTECTED;

// 테이블 모양만 정해요. 넣고 빼는 건 FollowJpaRepository의 쿼리로 해요.
@Entity
@Table(name = "follows", indexes = @Index(columnList = "followee_id"))
@IdClass(FollowJpaEntity.Key.class)
@NoArgsConstructor(access = PROTECTED)
class FollowJpaEntity {
    @Id
    private UUID followerId;

    @Id
    private UUID followeeId;

    @Column(nullable = false)
    private Instant createdAt;

    record Key(UUID followerId, UUID followeeId) implements Serializable {
    }
}
