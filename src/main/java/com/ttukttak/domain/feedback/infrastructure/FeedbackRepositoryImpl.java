package com.ttukttak.domain.feedback.infrastructure;

import com.ttukttak.domain.feedback.domain.Feedback;
import com.ttukttak.domain.feedback.domain.FeedbackRepository;
import com.ttukttak.domain.feedback.domain.FeedbackSort;
import com.ttukttak.domain.feedback.domain.FeedbackType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
class FeedbackRepositoryImpl implements FeedbackRepository {
    private final FeedbackJpaRepository jpaRepository;

    @Override
    public Feedback save(Feedback feedback) {
        return jpaRepository.saveAndFlush(FeedbackJpaEntity.from(feedback)).toDomain();
    }

    @Override
    public Optional<Feedback> findById(UUID id) {
        return jpaRepository.findById(id).map(FeedbackJpaEntity::toDomain);
    }

    @Override
    public Optional<Feedback> findByIdForUpdate(UUID id) {
        return jpaRepository.findByIdForUpdate(id).map(FeedbackJpaEntity::toDomain);
    }

    @Override
    public Optional<Feedback> findByIdForShare(UUID id) {
        return jpaRepository.findByIdForShare(id).map(FeedbackJpaEntity::toDomain);
    }

    // 부르는 쪽이 findByIdForUpdate로 이미 잠갔어요.
    @Override
    public void delete(UUID id) {
        jpaRepository.deleteHelpful(id);
        jpaRepository.deleteById(id);
    }

    @Override
    public void deleteBySubmissionId(UUID submissionId) {
        jpaRepository.lockBySubmissionId(submissionId);
        jpaRepository.deleteHelpfulBySubmissionId(submissionId);
        jpaRepository.deleteBySubmissionId(submissionId);
    }

    @Override
    public List<Feedback> findPage(UUID submissionId, FeedbackType type, FeedbackSort sort, int offset, int limit) {
        return jpaRepository.findPage(submissionId, type == null ? null : type.name(), sort.name(), offset, limit)
                .stream().map(FeedbackJpaEntity::toDomain).toList();
    }

    @Override
    public Map<FeedbackType, Long> countByType(UUID submissionId) {
        return jpaRepository.countByType(submissionId).stream().collect(Collectors.toMap(
                row -> FeedbackType.valueOf((String) row[0]), row -> ((Number) row[1]).longValue()));
    }

    @Override
    public void markHelpful(UUID feedbackId, UUID userId) {
        jpaRepository.markHelpful(feedbackId, userId);
    }

    @Override
    public void unmarkHelpful(UUID feedbackId, UUID userId) {
        jpaRepository.unmarkHelpful(feedbackId, userId);
    }

    @Override
    public Map<UUID, Long> helpfulCounts(Collection<UUID> feedbackIds) {
        if (feedbackIds.isEmpty()) return Map.of();
        return jpaRepository.helpfulCounts(feedbackIds).stream().collect(Collectors.toMap(
                row -> (UUID) row[0], row -> ((Number) row[1]).longValue()));
    }

    @Override
    public Set<UUID> findHelpful(UUID userId, Collection<UUID> feedbackIds) {
        return feedbackIds.isEmpty() ? Set.of() : Set.copyOf(jpaRepository.findHelpful(userId, feedbackIds));
    }

    @Override
    public List<Feedback> findPageByUserId(UUID userId, int offset, int limit) {
        return jpaRepository.findPageByUserId(userId, offset, limit).stream().map(FeedbackJpaEntity::toDomain).toList();
    }

    @Override
    public List<Instant> findCreatedAtByUserId(UUID userId) {
        return jpaRepository.findCreatedAtByUserId(userId);
    }

    @Override
    public long points(UUID userId) {
        return jpaRepository.points(userId);
    }
}
