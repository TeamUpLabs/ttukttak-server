package com.ttukttak.auth.presentation.response;

import com.ttukttak.auth.presentation.common.UserSummary;
import com.ttukttak.domain.user.domain.User;

import java.util.UUID;

/** GET /me 응답. 진행 중인 주차가 없으면 currentWeek가 null이에요. */
public record MeResponse(UserSummary user, String role, CurrentWeek currentWeek, int points) {

    /** submissionId가 있으면 제출하기 화면을 수정 모드로 열어요. */
    public record CurrentWeek(String weekId, boolean joined, UUID submissionId, int streak) {
    }

    public static MeResponse from(User user, CurrentWeek currentWeek) {
        return new MeResponse(UserSummary.from(user), user.getRole().name().toLowerCase(), currentWeek,
                user.getPoints());
    }
}
