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

    Optional<SubmissionJpaEntity> findByWeekIdAndUserId(String weekId, UUID userId);

    // ponytail: offset 페이지라 투표 중에 순서가 바뀌면 겹치거나 빠질 수 있어요. 문제되면 키셋 커서로.
    // ponytail: 투표가 아직 없어서 VOTES는 최신순과 같아요. 투표를 만들 때 vote_count desc를 앞에 붙여요.
    @Query(value = "select s.* " + VISIBLE + """
            order by
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

    @Query("select s.weekId from SubmissionJpaEntity s where s.userId = :userId")
    List<String> findWeekIdsByUserId(UUID userId);
}
