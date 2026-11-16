package com.ttukttak.domain.feedback.infrastructure;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// 탈퇴한 사용자의 피드백은 목록에서 빠져요.
interface FeedbackJpaRepository extends JpaRepository<FeedbackJpaEntity, UUID> {
    String VISIBLE = """
            from feedback f join users u on u.id = f.user_id and u.deleted_at is null
            where f.submission_id = :submissionId
            """;

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from FeedbackJpaEntity f where f.id = :id")
    Optional<FeedbackJpaEntity> findByIdForUpdate(UUID id);

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("select f from FeedbackJpaEntity f where f.id = :id")
    Optional<FeedbackJpaEntity> findByIdForShare(UUID id);

    // HELPFUL은 도움돼요 많은 순, 같으면 최신순이에요.
    @Query(value = "select f.* " + VISIBLE + """
              and (cast(:type as text) is null or f.type = cast(:type as text))
            order by
              case when :sort = 'HELPFUL' then (select count(*) from feedback_helpful h where h.feedback_id = f.id) end desc,
              f.created_at desc, f.id
            offset :offset limit :limit""", nativeQuery = true)
    List<FeedbackJpaEntity> findPage(UUID submissionId, String type, String sort, int offset, int limit);

    @Query(value = "select f.type, count(*) " + VISIBLE + "group by f.type", nativeQuery = true)
    List<Object[]> countByType(UUID submissionId);

    @Modifying
    @Query(value = "delete from feedback_helpful where feedback_id = :feedbackId", nativeQuery = true)
    void deleteHelpful(UUID feedbackId);

    // 도움돼요(공유 잠금)가 끝나길 기다렸다가 잠가서, 지우는 사이에 새 도움돼요가 끼지 않게 해요.
    @Query(value = "select id from feedback where submission_id = :submissionId for update", nativeQuery = true)
    List<UUID> lockBySubmissionId(UUID submissionId);

    @Modifying
    @Query(value = """
            delete from feedback_helpful where feedback_id in (select id from feedback where submission_id = :submissionId)""",
            nativeQuery = true)
    void deleteHelpfulBySubmissionId(UUID submissionId);

    @Modifying
    @Query("delete from FeedbackJpaEntity f where f.submissionId = :submissionId")
    void deleteBySubmissionId(UUID submissionId);

    @Modifying
    @Query(value = """
            insert into feedback_helpful (feedback_id, user_id, created_at) values (:feedbackId, :userId, now())
            on conflict do nothing""", nativeQuery = true)
    void markHelpful(UUID feedbackId, UUID userId);

    @Modifying
    @Query(value = "delete from feedback_helpful where feedback_id = :feedbackId and user_id = :userId", nativeQuery = true)
    void unmarkHelpful(UUID feedbackId, UUID userId);

    @Query(value = """
            select feedback_id, count(*) from feedback_helpful where feedback_id in :feedbackIds group by feedback_id""",
            nativeQuery = true)
    List<Object[]> helpfulCounts(Collection<UUID> feedbackIds);

    @Query(value = "select feedback_id from feedback_helpful where user_id = :userId and feedback_id in :feedbackIds",
            nativeQuery = true)
    List<UUID> findHelpful(UUID userId, Collection<UUID> feedbackIds);

    @Query(value = """
            select * from feedback where user_id = :userId order by created_at desc, id offset :offset limit :limit""",
            nativeQuery = true)
    List<FeedbackJpaEntity> findPageByUserId(UUID userId, int offset, int limit);

    @Query("select f.createdAt from FeedbackJpaEntity f where f.userId = :userId order by f.createdAt")
    List<Instant> findCreatedAtByUserId(UUID userId);

    // ponytail: 볼 때마다 계산해요. 명세의 point_ledger(원장)는 포인트 규칙이 늘어나면 둬요.
    @Query(value = """
            select coalesce((select sum(least(100, 10 * n)) from (
                       select count(*) n from feedback f join submissions s on s.id = f.submission_id
                       where f.user_id = :userId and f.earns_points group by s.week_id) weekly), 0)
                 + 2 * (select count(*) from feedback_helpful h join feedback f on f.id = h.feedback_id
                        where f.user_id = :userId)""", nativeQuery = true)
    long points(UUID userId);
}
