package com.ttukttak.domain.feedback.domain;

import java.util.*;

public interface FeedbackRepository {
    Feedback save(Feedback feedback);

    Optional<Feedback> findById(UUID id);

    /** 트랜잭션이 끝날 때까지 행을 잠가요. 수정·답글·삭제가 서로 덮어쓰지 않게 해요. */
    Optional<Feedback> findByIdForUpdate(UUID id);

    /** 같이 읽는 건 막지 않고 findByIdForUpdate(삭제)와만 줄을 서요. 도움돼요가 삭제 사이에 끼지 않게 해요. */
    Optional<Feedback> findByIdForShare(UUID id);

    /** 받은 도움돼요도 함께 지워요. */
    void delete(UUID id);

    /** 제출작 철회: 피드백과 받은 도움돼요를 모두 지워요. */
    void deleteBySubmissionId(UUID submissionId);

    /** type은 null이면 거르지 않아요. */
    List<Feedback> findPage(UUID submissionId, FeedbackType type, FeedbackSort sort, int offset, int limit);

    /** 종류별 개수. 없는 종류는 빠져요. */
    Map<FeedbackType, Long> countByType(UUID submissionId);

    /** 여러 번 불러도 한 번만 세요. */
    void markHelpful(UUID feedbackId, UUID userId);

    void unmarkHelpful(UUID feedbackId, UUID userId);

    /** 피드백 id → 도움돼요 수. 없으면 빠져요. */
    Map<UUID, Long> helpfulCounts(Collection<UUID> feedbackIds);

    /** feedbackIds 중 userId가 도움돼요를 누른 것 */
    Set<UUID> findHelpful(UUID userId, Collection<UUID> feedbackIds);

    /** 피드백 1개당 10점(주차마다 최대 100점, 포인트가 쌓이는 것만) + 받은 도움돼요 1개당 2점 */
    long points(UUID userId);
}
