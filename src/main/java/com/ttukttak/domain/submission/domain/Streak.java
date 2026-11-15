package com.ttukttak.domain.submission.domain;

import com.ttukttak.domain.week.domain.Phase;
import com.ttukttak.domain.week.domain.Week;

import java.time.Instant;
import java.util.List;
import java.util.Set;

/** 연속 제출 주 수. 휴식 주는 끊기지도, 늘지도 않아요. */
public record Streak(int current, int best) {

    /** themes: 이미 열린 주제 주, 최신순. submitted: 제출한 주차 id */
    public static Streak of(List<Week> themes, Set<String> submitted, Instant now) {
        int current = -1, best = 0, run = 0;
        for (int i = 0; i < themes.size(); i++) {
            Week week = themes.get(i);
            if (submitted.contains(week.getId())) {
                best = Math.max(best, ++run);
                continue;
            }
            // 이번 주가 진행 중이고 아직 안 냈으면 지난주까지로 계산해요.
            if (i == 0 && week.phase(now) == Phase.BUILDING) continue;
            if (current < 0) current = run;
            run = 0;
        }
        return new Streak(current < 0 ? run : current, best);
    }
}
