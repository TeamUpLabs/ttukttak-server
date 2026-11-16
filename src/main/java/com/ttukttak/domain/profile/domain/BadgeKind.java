package com.ttukttak.domain.profile.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public enum BadgeKind {
    STREAK("연속 제출", "WEEKS", List.of(3, 5, 7, 10, 20)),
    PODIUM("트랙 TOP 3", "TIMES", List.of(1, 3, 5, 10)),
    FEEDBACK("남긴 피드백", "COUNT", List.of(10, 30, 60, 100));

    private final String description;
    private final String unit;
    private final List<Integer> thresholds;
}
