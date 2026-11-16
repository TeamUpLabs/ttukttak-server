package com.ttukttak.domain.user.domain;

import java.util.UUID;

public interface FollowRepository {
    /** 여러 번 불러도 한 번만 세요. */
    void follow(UUID followerId, UUID followeeId);

    void unfollow(UUID followerId, UUID followeeId);

    boolean isFollowing(UUID followerId, UUID followeeId);

    /** 탈퇴한 사용자는 빼고 세요. */
    long countFollowers(UUID userId);

    long countFollowing(UUID userId);
}
