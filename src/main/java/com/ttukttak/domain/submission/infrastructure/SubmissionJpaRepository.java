package com.ttukttak.domain.submission.infrastructure;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// 탈퇴한 사용자의 제출작은 목록에서 빠져요.
interface SubmissionJpaRepository extends JpaRepository<SubmissionJpaEntity, UUID> {
    String VISIBLE = """
            from submissions s join users u on u.id = s.user_id and u.deleted_at is null
            where s.week_id = :weekId
              and (cast(:track as text) is null or s.track = cast(:track as text))
              and (cast(:exclude as uuid) is null or s.id <> cast(:exclude as uuid))
            """;

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SubmissionJpaEntity s where s.id = :id")
    Optional<SubmissionJpaEntity> findByIdForUpdate(UUID id);

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("select s from SubmissionJpaEntity s where s.id = :id")
    Optional<SubmissionJpaEntity> findByIdForShare(UUID id);

    Optional<SubmissionJpaEntity> findByWeekIdAndUserId(String weekId, UUID userId);

    // VOTES는 표 많은 순, 동점이면 먼저 낸 순이에요.
    @Query(value = "select s.* " + VISIBLE + """
            order by
              case when :sort = 'VOTES' then (select count(*) from votes v where v.submission_id = s.id) end desc,
              case when :sort = 'VOTES' then s.submitted_at end,
              case when :sort = 'RANDOM' then md5(s.id::text || :seed) end,
              s.submitted_at desc, s.id
            offset :offset limit :limit""", nativeQuery = true)
    List<SubmissionJpaEntity> findPage(String weekId, String track, UUID exclude, String sort, String seed,
                                       int offset, int limit);

    @Query(value = "select count(*) " + VISIBLE, nativeQuery = true)
    long count(String weekId, String track, UUID exclude);

    @Query(value = """
            select s.week_id, count(*) from submissions s join users u on u.id = s.user_id and u.deleted_at is null
            where s.week_id in :weekIds group by s.week_id""", nativeQuery = true)
    List<Object[]> countByWeekIds(Collection<String> weekIds);

    List<SubmissionJpaEntity> findByUserIdOrderBySubmittedAtDesc(UUID userId);

    // Scoreboard와 같은 순서예요.
    @Query(value = """
            select id, track_rank from (
              select s.id, s.user_id, row_number() over (
                       partition by s.week_id, s.track
                       order by (select count(*) from votes v where v.submission_id = s.id) desc, s.submitted_at, s.id
                     ) track_rank
              from submissions s join users u on u.id = s.user_id and u.deleted_at is null
              where s.week_id in (select week_id from submissions where user_id = :userId)
            ) ranked where user_id = :userId""", nativeQuery = true)
    List<Object[]> trackRanks(UUID userId);

    @Query(value = """
            select count(*) from votes v join submissions s on s.id = v.submission_id where s.user_id = :userId""",
            nativeQuery = true)
    long votesReceived(UUID userId);

    @Query("select s.weekId from SubmissionJpaEntity s where s.userId = :userId")
    List<String> findWeekIdsByUserId(UUID userId);
}
