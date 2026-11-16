package com.ttukttak.profile.presentation.response;

import com.ttukttak.auth.presentation.common.UserLinks;
import com.ttukttak.auth.presentation.common.UserSummary;
import com.ttukttak.domain.user.domain.User;
import com.ttukttak.week.presentation.common.WeekSchedule;

import java.time.OffsetDateTime;
import java.util.UUID;

/** GET /users/:handle 응답. 비로그인이면 viewer가 null이에요. */
public record ProfileResponse(Profile user, Stats stats, Social social, Viewer viewer) {

    public record Profile(UUID id, String handle, String name, String avatarUrl, String initials,
                          String bio, UserLinks links, OffsetDateTime joinedAt) {
        public static Profile from(User user) {
            return new Profile(user.getId(), user.getHandle(), user.getName(), user.getAvatarUrl(),
                    UserSummary.initials(user.getHandle()), user.getBio(),
                    new UserLinks(user.getGithubLink(), user.getBlogLink()), WeekSchedule.kst(user.getJoinedAt()));
        }
    }

    /** submissionsLast52: 52주 격자 안의 제출 수, top3Count: 결과 발표 뒤 트랙 3위 안에 든 횟수 */
    public record Stats(int currentStreak, int bestStreak, long submissionsLast52, long top3Count,
                        long votesReceived, long feedbackGiven) {
    }

    public record Social(long followers, long following) {
    }

    public record Viewer(boolean following, boolean isSelf) {
    }
}
