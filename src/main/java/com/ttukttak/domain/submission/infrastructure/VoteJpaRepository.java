package com.ttukttak.domain.submission.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

interface VoteJpaRepository extends JpaRepository<VoteJpaEntity, VoteJpaEntity.Key> {
    @Modifying
    @Query(value = """
            insert into votes (submission_id, user_id, created_at) values (:submissionId, :userId, now())
            on conflict do nothing""", nativeQuery = true)
    void vote(UUID submissionId, UUID userId);

    @Modifying
    @Query("delete from VoteJpaEntity v where v.submissionId = :submissionId and v.userId = :userId")
    void unvote(UUID submissionId, UUID userId);

    @Modifying
    @Query("delete from VoteJpaEntity v where v.submissionId = :submissionId")
    void deleteBySubmissionId(UUID submissionId);

    long countBySubmissionId(UUID submissionId);

    // ponytail: 볼 때마다 표를 세요. 느려지면 submissions.vote_count 캐시 칸을 둬요.
    @Query(value = """
            select s.id, s.track, count(v.user_id)
            from submissions s join users u on u.id = s.user_id and u.deleted_at is null
            left join votes v on v.submission_id = s.id
            where s.week_id = :weekId
            group by s.id
            order by count(v.user_id) desc, s.submitted_at, s.id""", nativeQuery = true)
    List<Object[]> totals(String weekId);

    @Query("select v.submissionId from VoteJpaEntity v where v.userId = :userId and v.submissionId in :submissionIds")
    List<UUID> findVoted(UUID userId, Collection<UUID> submissionIds);
}
