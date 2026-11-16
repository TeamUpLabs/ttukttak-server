package com.ttukttak.week.application;

import com.ttukttak.domain.submission.application.query.SubmissionQueryService;
import com.ttukttak.domain.week.application.command.WeekCommandService;
import com.ttukttak.submission.application.SubmissionFacade;
import com.ttukttak.domain.week.application.query.WeekQueryService;
import com.ttukttak.domain.week.domain.Week;
import com.ttukttak.week.presentation.request.ImportWeeksRequest;
import com.ttukttak.week.presentation.request.UpdateWeekRequest;
import com.ttukttak.week.presentation.response.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** 요청·응답 DTO와 도메인 사이를 이어줘요. 트랜잭션은 각 서비스가 가져요. */
@Component
@RequiredArgsConstructor
public class WeekFacade {
    private static final int MAX_LIMIT = 50;

    private final WeekCommandService weekCommandService;
    private final WeekQueryService weekQueryService;
    private final SubmissionQueryService submissionQueryService;
    private final SubmissionFacade submissionFacade;

    public CurrentWeekResponse current() {
        Instant now = Instant.now();
        Week week = weekQueryService.current(now).orElse(null);
        long participants = week == null ? 0 : weekQueryService.participants(week.getId());
        long submissions = week == null ? 0 : submissions(week.getId());
        return CurrentWeekResponse.of(week, participants, submissions, weekQueryService.next(now).orElse(null), now);
    }

    public WeekListResponse archive(String cursor, int limit) {
        Instant now = Instant.now();
        int size = Math.clamp(limit, 1, MAX_LIMIT);
        List<Week> weeks = weekQueryService.archive(now, cursor, size);
        boolean hasNext = weeks.size() > size;
        List<Week> page = hasNext ? weeks.subList(0, size) : weeks;
        Map<String, Long> counts = submissionQueryService.counts(page.stream().map(Week::getId).toList());
        return new WeekListResponse(
                page.stream().map(week -> WeekSummary.from(week, counts.getOrDefault(week.getId(), 0L), now)).toList(),
                hasNext ? page.getLast().getId() : null);
    }

    public WeekDetailResponse get(String id, UUID viewerId) {
        Instant now = Instant.now();
        Week week = weekQueryService.get(id);
        return WeekDetailResponse.of(week, weekQueryService.participants(id), submissions(id),
                submissionQueryService.scoreboard(id).totalVotes(), submissionFacade.podium(week, viewerId),
                weekQueryService.prevTheme(id).orElse(null), weekQueryService.nextTheme(id).orElse(null), now);
    }

    public PressResponse press(int count) {
        return new PressResponse(weekCommandService.press(count, Instant.now()));
    }

    public ParticipationResponse join(UUID userId) {
        return new ParticipationResponse(true, weekCommandService.join(userId, Instant.now()));
    }

    public ParticipationResponse leave(UUID userId) {
        return new ParticipationResponse(false, weekCommandService.leave(userId, Instant.now()));
    }

    public WeekDetailResponse update(String id, UpdateWeekRequest request) {
        weekCommandService.update(id, request::applyTo, request.forced(), Instant.now());
        return get(id, null);
    }

    public void announce(String id) {
        weekCommandService.announce(id);
    }

    public void importWeeks(ImportWeeksRequest request) {
        weekCommandService.importWeeks(request.toDomain());
    }

    private long submissions(String weekId) {
        return submissionQueryService.count(weekId, null, null);
    }
}
