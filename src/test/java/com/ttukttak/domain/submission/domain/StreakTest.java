package com.ttukttak.domain.submission.domain;

import com.ttukttak.domain.week.domain.Week;
import com.ttukttak.domain.week.domain.WeekType;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class StreakTest {
    static final Instant NOW = Instant.parse("2026-01-06T00:00:00Z");

    @Test
    void streak() {
        // 최신순: w04(제작 중), w03, w02, w01
        List<Week> themes = List.of(week("w04", 1), week("w03", -6), week("w02", -13), week("w01", -20));

        // 이번 주를 아직 안 냈으면 지난주까지
        assertThat(Streak.of(themes, Set.of("w03", "w02"), NOW)).isEqualTo(new Streak(2, 2, "w02"));
        assertThat(Streak.of(themes, Set.of("w04", "w03", "w02"), NOW)).isEqualTo(new Streak(3, 3, "w02"));
        // 한 주 빠지면 끊겨요. 최고 기록은 남아요.
        assertThat(Streak.of(themes, Set.of("w04", "w02", "w01"), NOW)).isEqualTo(new Streak(1, 2, "w04"));
        // 마감이 지난 주를 안 냈으면 0
        assertThat(Streak.of(themes.subList(1, 4), Set.of("w02", "w01"), NOW)).isEqualTo(new Streak(0, 2, null));
        assertThat(Streak.of(List.of(), Set.of(), NOW)).isEqualTo(new Streak(0, 0, null));
    }

    /** closesInDays일 뒤(음수면 전)에 제출이 마감되는 주. 3일 제작, 3일 투표. */
    private static Week week(String id, int closesInDays) {
        Instant closes = NOW.plus(Duration.ofDays(closesInDays));
        return Week.builder().id(id).type(WeekType.THEME).opensAt(closes.minus(Duration.ofDays(3)))
                .submissionClosesAt(closes).votingClosesAt(closes.plus(Duration.ofDays(3)))
                .resultsAt(closes.plus(Duration.ofDays(4))).build();
    }
}
