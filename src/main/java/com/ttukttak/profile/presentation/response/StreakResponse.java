package com.ttukttak.profile.presentation.response;

import com.ttukttak.domain.profile.domain.StreakGrid;

import java.util.List;

/** 52주 기록 격자, 오래된 주부터 이번 주까지. current.fromWeekId: 지금 이어지는 기록의 첫 주 */
public record StreakResponse(List<StreakGrid.Cell> weeks, Current current) {

    public record Current(int length, String fromWeekId) {
    }
}
