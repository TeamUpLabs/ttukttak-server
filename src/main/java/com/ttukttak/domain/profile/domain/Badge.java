package com.ttukttak.domain.profile.domain;

import com.ttukttak.domain.week.domain.Week;

import java.time.Instant;
import java.util.*;

/** 기록으로 계산하는 배지. 이름·설명·색은 프론트가 kind와 value로 정해요. */
// ponytail: 저장하지 않아서 제출을 철회하거나 피드백을 지우면 배지도 사라져요. 영구 보존이 필요하면 badges 테이블에 쌓아요.
public record Badge(BadgeKind kind, int value, Instant earnedAt) {

    public String id() {
        return kind.name().toLowerCase() + "-" + value;
    }

    /**
     * themes: 열린 주제 주, 오래된 순. submittedAt: 주차 id → 제출 시각.
     * podiumAt: TOP 3에 든 주차의 결과 발표 시각, feedbackAt: 남긴 피드백 시각. 둘 다 오래된 순.
     */
    public static List<Badge> of(List<Week> themes, Map<String, Instant> submittedAt,
                                 List<Instant> podiumAt, List<Instant> feedbackAt) {
        List<Badge> badges = new ArrayList<>();
        // 연속 제출은 기록이 그 길이에 처음 닿은 주에 받아요.
        Set<Integer> earned = new HashSet<>();
        int run = 0;
        for (Week week : themes) {
            Instant at = submittedAt.get(week.getId());
            run = at == null ? 0 : run + 1;
            if (BadgeKind.STREAK.getThresholds().contains(run) && earned.add(run)) {
                badges.add(new Badge(BadgeKind.STREAK, run, at));
            }
        }
        badges.addAll(counted(BadgeKind.PODIUM, podiumAt));
        badges.addAll(counted(BadgeKind.FEEDBACK, feedbackAt));
        badges.sort(Comparator.comparing(Badge::earnedAt));
        return badges;
    }

    // n번째가 생긴 순간에 받아요.
    private static List<Badge> counted(BadgeKind kind, List<Instant> at) {
        return kind.getThresholds().stream().filter(n -> at.size() >= n)
                .map(n -> new Badge(kind, n, at.get(n - 1))).toList();
    }
}
