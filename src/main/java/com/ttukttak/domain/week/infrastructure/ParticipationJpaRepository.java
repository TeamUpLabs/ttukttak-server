package com.ttukttak.domain.week.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

interface ParticipationJpaRepository extends JpaRepository<ParticipationJpaEntity, ParticipationJpaEntity.Key> {
    // 이미 있으면 바꾸는 것 없이 행만 잠가요. 동시에 온 참가 취소가 이 트랜잭션이 끝날 때까지 기다려요.
    @Modifying
    @Query(value = """
            insert into participations (week_id, user_id, joined_at) values (:weekId, :userId, now())
            on conflict (week_id, user_id) do update set joined_at = participations.joined_at""", nativeQuery = true)
    void join(String weekId, UUID userId);

    @Modifying
    @Query("delete from ParticipationJpaEntity p where p.weekId = :weekId and p.userId = :userId")
    void leave(String weekId, UUID userId);

    boolean existsByWeekIdAndUserId(String weekId, UUID userId);

    long countByWeekId(String weekId);
}
