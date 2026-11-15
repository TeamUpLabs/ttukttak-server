package com.ttukttak.domain.submission.infrastructure;

import com.ttukttak.domain.submission.domain.Submission;
import com.ttukttak.domain.submission.domain.SubmissionRepository;
import com.ttukttak.domain.submission.domain.SubmissionSort;
import com.ttukttak.domain.submission.domain.Track;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
class SubmissionRepositoryImpl implements SubmissionRepository {
    private final SubmissionJpaRepository jpaRepository;

    @Override
    public Submission save(Submission submission) {
        return jpaRepository.saveAndFlush(SubmissionJpaEntity.from(submission)).toDomain();
    }

    @Override
    public Optional<Submission> findById(UUID id) {
        return jpaRepository.findById(id).map(SubmissionJpaEntity::toDomain);
    }

    @Override
    public Optional<Submission> findByWeekIdAndUserId(String weekId, UUID userId) {
        return jpaRepository.findByWeekIdAndUserId(weekId, userId).map(SubmissionJpaEntity::toDomain);
    }

    @Override
    public void delete(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<Submission> findPage(String weekId, Track track, UUID exclude, SubmissionSort sort, String seed,
                                     int offset, int limit) {
        return jpaRepository.findPage(weekId, track == null ? null : track.name(), exclude, sort.name(),
                        seed == null ? "" : seed, offset, limit)
                .stream().map(SubmissionJpaEntity::toDomain).toList();
    }

    @Override
    public long count(String weekId, Track track, UUID exclude) {
        return jpaRepository.count(weekId, track == null ? null : track.name(), exclude);
    }

    @Override
    public Map<String, Long> countByWeekIds(Collection<String> weekIds) {
        return jpaRepository.countByWeekIds(weekIds).stream()
                .collect(Collectors.toMap(row -> (String) row[0], row -> ((Number) row[1]).longValue()));
    }

    @Override
    public List<String> findWeekIdsByUserId(UUID userId) {
        return jpaRepository.findWeekIdsByUserId(userId);
    }
}
