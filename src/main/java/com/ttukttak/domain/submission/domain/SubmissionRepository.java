package com.ttukttak.domain.submission.domain;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface SubmissionRepository {
    Submission save(Submission submission);

    Optional<Submission> findById(UUID id);

    /** 트랜잭션이 끝날 때까지 행을 잠가요. 동시에 고쳐도 앞 수정을 덮어쓰지 않게 해요. */
    Optional<Submission> findByIdForUpdate(UUID id);

    /** 같이 읽는 건 막지 않고 findByIdForUpdate(철회)와만 줄을 서요. 투표가 철회 사이에 끼지 않게 해요. */
    Optional<Submission> findByIdForShare(UUID id);

    Optional<Submission> findByWeekIdAndUserId(String weekId, UUID userId);

    void delete(UUID id);

    /** track, exclude는 null이면 거르지 않아요. seed는 RANDOM일 때 순서를 고정해요. */
    List<Submission> findPage(String weekId, Track track, UUID exclude, SubmissionSort sort, String seed,
                              int offset, int limit);

    long count(String weekId, Track track, UUID exclude);

    /** 주차 id → 제출 수. 제출이 없는 주차는 빠져요. */
    Map<String, Long> countByWeekIds(Collection<String> weekIds);

    List<String> findWeekIdsByUserId(UUID userId);

    /** 최신순 */
    List<Submission> findByUserId(UUID userId);

    List<Submission> findAllById(Collection<UUID> ids);

    /** userId의 제출작 id → 그 주 트랙 안 순위(표 많은 순, 동점이면 먼저 낸 순) */
    Map<UUID, Integer> trackRanks(UUID userId);

    /** userId의 제출작들이 받은 표 */
    long votesReceived(UUID userId);
}
