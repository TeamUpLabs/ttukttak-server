package com.ttukttak.domain.profile.domain;

import com.ttukttak.domain.week.domain.Week;
import com.ttukttak.domain.week.domain.WeekType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class BadgeTest {
    static final Instant T = Instant.parse("2026-01-01T00:00:00Z");

    @Test
    void badges() {
        List<Week> themes = IntStream.rangeClosed(1, 8).mapToObj(i -> theme("w" + i)).toList();
        // w1–w3 연속, w4 빠짐, w5–w8 연속: 3주 배지는 w3에서 한 번만
        Map<String, Instant> submitted = Map.of("w1", at(1), "w2", at(2), "w3", at(3),
                "w5", at(5), "w6", at(6), "w7", at(7), "w8", at(8));
        List<Instant> feedback = IntStream.range(0, 30).mapToObj(BadgeTest::at).toList();

        List<Badge> badges = Badge.of(themes, submitted, List.of(at(4)), feedback);

        assertThat(badges).extracting(Badge::id)
                .containsExactlyInAnyOrder("streak-3", "podium-1", "feedback-10", "feedback-30");
        assertThat(badges).filteredOn(b -> b.id().equals("streak-3")).extracting(Badge::earnedAt).containsExactly(at(3));
        assertThat(badges).filteredOn(b -> b.id().equals("feedback-30")).extracting(Badge::earnedAt).containsExactly(at(29));
    }

    private static Instant at(int day) {
        return T.plusSeconds(86400L * day);
    }

    private static Week theme(String id) {
        return Week.builder().id(id).type(WeekType.THEME).build();
    }
}
