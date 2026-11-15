package com.ttukttak.domain.submission.application.query;

import com.ttukttak.domain.submission.domain.*;
import com.ttukttak.domain.submission.exception.SubmissionNotFoundException;
import com.ttukttak.domain.week.domain.WeekRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubmissionQueryService {
    private final SubmissionRepository submissionRepository;
    private final WeekRepository weekRepository;

    public Submission get(UUID id) {
        return submissionRepository.findById(id).orElseThrow(SubmissionNotFoundException::new);
    }

    public Optional<Submission> find(String weekId, UUID userId) {
        return submissionRepository.findByWeekIdAndUserId(weekId, userId);
    }

    public List<Submission> page(String weekId, Track track, UUID exclude, SubmissionSort sort, String seed,
                                 int offset, int limit) {
        return submissionRepository.findPage(weekId, track, exclude, sort, seed, offset, limit);
    }

    public long count(String weekId, Track track, UUID exclude) {
        return submissionRepository.count(weekId, track, exclude);
    }

    public Map<String, Long> counts(Collection<String> weekIds) {
        return weekIds.isEmpty() ? Map.of() : submissionRepository.countByWeekIds(weekIds);
    }

    // ponytail: 볼 때마다 전체 주차를 훑어요. 주차가 수백 개가 되면 users.current_streak에 저장해요.
    public Streak streak(UUID userId, Instant now) {
        return Streak.of(weekRepository.findOpenedThemes(now),
                Set.copyOf(submissionRepository.findWeekIdsByUserId(userId)), now);
    }
}
