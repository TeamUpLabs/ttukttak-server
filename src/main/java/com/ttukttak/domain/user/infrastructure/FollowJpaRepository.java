package com.ttukttak.domain.user.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

interface FollowJpaRepository extends JpaRepository<FollowJpaEntity, FollowJpaEntity.Key> {
    @Modifying
    @Query(value = """
            insert into follows (follower_id, followee_id, created_at) values (:followerId, :followeeId, now())
            on conflict do nothing""", nativeQuery = true)
    void follow(UUID followerId, UUID followeeId);

    @Modifying
    @Query("delete from FollowJpaEntity f where f.followerId = :followerId and f.followeeId = :followeeId")
    void unfollow(UUID followerId, UUID followeeId);

    boolean existsByFollowerIdAndFolloweeId(UUID followerId, UUID followeeId);

    @Query(value = """
            select count(*) from follows f join users u on u.id = f.follower_id and u.deleted_at is null
            where f.followee_id = :userId""", nativeQuery = true)
    long countFollowers(UUID userId);

    @Query(value = """
            select count(*) from follows f join users u on u.id = f.followee_id and u.deleted_at is null
            where f.follower_id = :userId""", nativeQuery = true)
    long countFollowing(UUID userId);
}
