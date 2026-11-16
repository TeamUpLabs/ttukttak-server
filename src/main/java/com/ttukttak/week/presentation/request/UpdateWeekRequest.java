package com.ttukttak.week.presentation.request;

import com.ttukttak.domain.week.domain.Difficulty;
import com.ttukttak.domain.week.domain.Examples;
import com.ttukttak.domain.week.domain.Week;
import com.ttukttak.domain.week.domain.WeekStatus;
import com.ttukttak.week.presentation.common.WeekSchedule;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;

import java.util.List;

import static com.ttukttak.week.presentation.common.WeekSchedule.instant;

/**
 * 보낸 필드만 바뀌어요. schedule은 네 칸을 통째로 바꿔요.
 * 공개된 주차의 일정을 바꾸려면 force: true가 필요해요.
 */
// ponytail: 담당자(owner)는 저장하는 칸이 없어서 아직 못 바꿔요.
public record UpdateWeekRequest(
        @Pattern(regexp = "(?s).*\\S.*", message = "제목을 입력해 주세요.") String title,
        String announcement,
        String intent,
        String rule,
        List<String> constraints,
        Difficulty difficulty,
        WeekStatus status,
        Examples examples,
        @Valid WeekSchedule schedule,
        String notes,
        Boolean force // 원시 타입이면 Jackson 3가 빠진 칸을 거절해요
) {
    public boolean forced() {
        return Boolean.TRUE.equals(force);
    }

    public Week applyTo(Week week) {
        Week.WeekBuilder builder = week.toBuilder();
        if (title != null) builder.title(title);
        if (announcement != null) builder.announcement(announcement);
        if (intent != null) builder.intent(intent);
        if (rule != null) builder.rule(rule);
        if (constraints != null) builder.constraints(constraints);
        if (difficulty != null) builder.difficulty(difficulty);
        if (status != null) builder.status(status);
        if (examples != null) builder.examples(examples);
        if (notes != null) builder.notes(notes);
        if (schedule != null) {
            builder.opensAt(instant(schedule.opensAt()))
                    .submissionClosesAt(instant(schedule.submissionClosesAt()))
                    .votingClosesAt(instant(schedule.votingClosesAt()))
                    .resultsAt(instant(schedule.resultsAt()));
        }
        return builder.build();
    }
}
