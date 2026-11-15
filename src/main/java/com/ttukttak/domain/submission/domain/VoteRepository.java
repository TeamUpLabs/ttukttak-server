package com.ttukttak.domain.submission.domain;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** 한 사람이 한 제출작에 한 표 */
public interface VoteRepository {
    /** 여러 번 불러도 한 표만 세요. */
    void vote(UUID submissionId, UUID userId);

    void unvote(UUID submissionId, UUID userId);

    long count(UUID submissionId);

    void deleteBySubmissionId(UUID submissionId);

    /** 주차의 모든 제출작, 표 많은 순, 동점이면 먼저 낸 순 */
    List<VoteTotal> totals(String weekId);

    /** submissionIds 중 userId가 투표한 것 */
    Set<UUID> findVoted(UUID userId, Collection<UUID> submissionIds);
}
