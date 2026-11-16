package com.ttukttak.domain.user.application.query;

import com.ttukttak.domain.user.domain.FollowRepository;
import com.ttukttak.domain.user.domain.User;
import com.ttukttak.domain.user.domain.UserRepository;
import com.ttukttak.domain.user.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryService {
    private final UserRepository userRepository;
    private final FollowRepository followRepository;

    public User get(String handle) {
        return userRepository.findByHandle(handle.toLowerCase()).orElseThrow(UserNotFoundException::new);
    }

    public User get(UUID id) {
        return userRepository.findById(id).orElseThrow(UserNotFoundException::new);
    }

    public boolean isFollowing(UUID followerId, UUID followeeId) {
        return followRepository.isFollowing(followerId, followeeId);
    }

    public long followers(UUID userId) {
        return followRepository.countFollowers(userId);
    }

    public long following(UUID userId) {
        return followRepository.countFollowing(userId);
    }

    /** 탈퇴한 사용자는 빠져요. */
    public Map<UUID, User> getAll(Collection<UUID> ids) {
        return userRepository.findAllById(ids).stream().collect(Collectors.toMap(User::getId, Function.identity()));
    }
}
