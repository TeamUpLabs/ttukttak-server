package com.ttukttak.domain.profile.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 52주 격자의 한 칸 */
@Getter
@RequiredArgsConstructor
public enum WeekState {
    NONE("안 냄"),
    SUBMITTED("제출"),
    TOP3("트랙 3위 안"),
    BREAK("휴식 주"),
    ONGOING("진행 중");

    private final String description;
}
