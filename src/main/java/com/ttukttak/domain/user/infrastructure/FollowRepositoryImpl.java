package com.ttukttak.domain.user.infrastructure;

import com.ttukttak.domain.user.domain.FollowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
class FollowRepositoryImpl implements FollowRepository {
    private final FollowJpaRepository jpaRepository;

    @Override
    public void follow(UUID followerId, UUID followeeId) {
        jpaRepository.follow(followerId, followeeId);
    }

    @Override
    public void unfollow(UUID followerId, UUID followeeId) {
        jpaRepository.unfollow(followerId, followeeId);
    }

    @Override
    public boolean isFollowing(UUID followerId, UUID followeeId) {
        return jpaRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId);
    }

    @Override
    public long countFollowers(UUID userId) {
        return jpaRepository.countFollowers(userId);
    }

    @Override
    public long countFollowing(UUID userId) {
        return jpaRepository.countFollowing(userId);
    }
}
