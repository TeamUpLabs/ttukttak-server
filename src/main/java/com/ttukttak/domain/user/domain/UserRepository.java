package com.ttukttak.domain.user.domain;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    User save(User user);

    Optional<User> findById(UUID id);

    List<User> findAllById(Collection<UUID> ids);

    Optional<User> findByHandle(String handle);

    Optional<User> findByGithubId(Long githubId);

    boolean existsByHandle(String handle);
}
