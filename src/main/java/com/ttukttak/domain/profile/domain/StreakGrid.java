package com.ttukttak.domain.profile.domain;

import com.ttukttak.domain.week.domain.Phase;
import com.ttukttak.domain.week.domain.Week;
import com.ttukttak.domain.week.domain.WeekType;

import java.time.Instant;
import java.util.List;
import java.util.Set;

/** 프로필의 52주 기록 격자 */
public final class StreakGrid {
    public static final int WEEKS = 52;

    public record Cell(String weekId, String code, WeekState state) {
    }

    private StreakGrid() {
    }

    /** weeks: 이미 열린 주차(휴식 주 포함), 오래된 순. 마지막 칸이 이번 주예요. */
    public static List<Cell> of(List<Week> weeks, Set<String> submitted, Set<String> top3, Instant now) {
        return weeks.stream().map(week -> new Cell(week.getId(), week.getCode(),
                state(week, week == weeks.getLast(), submitted, top3, now))).toList();
    }

    private static WeekState state(Week week, boolean latest, Set<String> submitted, Set<String> top3, Instant now) {
        if (week.getType() == WeekType.BREAK) return WeekState.BREAK;
        if (top3.contains(week.getId())) return WeekState.TOP3;
        if (submitted.contains(week.getId())) return WeekState.SUBMITTED;
        if (latest && week.phase(now) == Phase.BUILDING) return WeekState.ONGOING;
        return WeekState.NONE;
    }
}
