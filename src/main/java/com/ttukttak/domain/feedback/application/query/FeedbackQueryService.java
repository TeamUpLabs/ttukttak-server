package com.ttukttak.domain.feedback.application.query;

import com.ttukttak.domain.feedback.domain.Feedback;
import com.ttukttak.domain.feedback.domain.FeedbackRepository;
import com.ttukttak.domain.feedback.domain.FeedbackSort;
import com.ttukttak.domain.feedback.domain.FeedbackType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedbackQueryService {
    private final FeedbackRepository feedbackRepository;

    public List<Feedback> page(UUID submissionId, FeedbackType type, FeedbackSort sort, int offset, int limit) {
        return feedbackRepository.findPage(submissionId, type, sort, offset, limit);
    }

    public Map<FeedbackType, Long> countByType(UUID submissionId) {
        return feedbackRepository.countByType(submissionId);
    }

    public long count(UUID submissionId) {
        return countByType(submissionId).values().stream().mapToLong(Long::longValue).sum();
    }

    public Map<UUID, Long> helpfulCounts(Collection<UUID> feedbackIds) {
        return feedbackRepository.helpfulCounts(feedbackIds);
    }

    /** 비로그인이면 비어 있어요. */
    public Set<UUID> helpful(UUID userId, Collection<UUID> feedbackIds) {
        return userId == null ? Set.of() : feedbackRepository.findHelpful(userId, feedbackIds);
    }

    public long points(UUID userId) {
        return feedbackRepository.points(userId);
    }
}
