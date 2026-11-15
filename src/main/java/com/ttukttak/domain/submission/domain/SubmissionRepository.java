package com.ttukttak.domain.submission.domain;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface SubmissionRepository {
    Submission save(Submission submission);

    Optional<Submission> findById(UUID id);

    Optional<Submission> findByWeekIdAndUserId(String weekId, UUID userId);

    void delete(UUID id);

    /** track, exclude는 null이면 거르지 않아요. seed는 RANDOM일 때 순서를 고정해요. */
    List<Submission> findPage(String weekId, Track track, UUID exclude, SubmissionSort sort, String seed,
                              int offset, int limit);

    long count(String weekId, Track track, UUID exclude);

    /** 주차 id → 제출 수. 제출이 없는 주차는 빠져요. */
    Map<String, Long> countByWeekIds(Collection<String> weekIds);

    List<String> findWeekIdsByUserId(UUID userId);
}
