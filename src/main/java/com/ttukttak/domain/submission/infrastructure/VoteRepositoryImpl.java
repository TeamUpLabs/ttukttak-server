package com.ttukttak.domain.submission.infrastructure;

import com.ttukttak.domain.submission.domain.Track;
import com.ttukttak.domain.submission.domain.VoteRepository;
import com.ttukttak.domain.submission.domain.VoteTotal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@RequiredArgsConstructor
class VoteRepositoryImpl implements VoteRepository {
    private final VoteJpaRepository jpaRepository;

    @Override
    public void vote(UUID submissionId, UUID userId) {
        jpaRepository.vote(submissionId, userId);
    }

    @Override
    public void unvote(UUID submissionId, UUID userId) {
        jpaRepository.unvote(submissionId, userId);
    }

    @Override
    public long count(UUID submissionId) {
        return jpaRepository.countBySubmissionId(submissionId);
    }

    @Override
    public void deleteBySubmissionId(UUID submissionId) {
        jpaRepository.deleteBySubmissionId(submissionId);
    }

    @Override
    public List<VoteTotal> totals(String weekId) {
        return jpaRepository.totals(weekId).stream()
                .map(row -> new VoteTotal((UUID) row[0], Track.valueOf((String) row[1]), ((Number) row[2]).longValue()))
                .toList();
    }

    @Override
    public Set<UUID> findVoted(UUID userId, Collection<UUID> submissionIds) {
        return submissionIds.isEmpty() ? Set.of() : Set.copyOf(jpaRepository.findVoted(userId, submissionIds));
    }
}
