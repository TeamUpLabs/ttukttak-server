package com.ttukttak.week.presentation.response;

import com.ttukttak.domain.week.domain.Phase;
import com.ttukttak.domain.week.domain.Week;
import com.ttukttak.domain.week.domain.WeekType;
import com.ttukttak.week.presentation.common.WeekSchedule;

import java.time.Instant;
import java.util.List;

/** GET /weeks/:weekId 응답. 공개 전 주차는 주제 문구가 null이에요. */
public record WeekDetailResponse(Detail week, Stats stats, Object podium, Neighbor prev, Neighbor next) {

    public record Detail(String id, String code, WeekType type, Integer round, String title,
                         WeekSchedule schedule, Phase phase, long submissionCount,
                         String announcement, String intent, List<String> constraints) {
    }

    // ponytail: votes, podium은 투표를 만들 때 채워요.
    /** completionRate = 제출 수 ÷ 참가하기 누른 수(0–1) */
    public record Stats(long submissions, long participants, long votes, double completionRate) {
    }

    public record Neighbor(String id, String code, String title, Phase phase) {
        static Neighbor from(Week week, Instant now) {
            return week == null ? null
                    : new Neighbor(week.getId(), week.getCode(), week.isOpen(now) ? week.getTitle() : null, week.phase(now));
        }
    }

    public static WeekDetailResponse of(Week week, long participants, long submissions, Week prev, Week next,
                                        Instant now) {
        boolean open = week.isOpen(now);
        WeekSummary summary = WeekSummary.from(week, submissions, now);
        return new WeekDetailResponse(
                new Detail(summary.id(), summary.code(), summary.type(), summary.round(), summary.title(),
                        summary.schedule(), summary.phase(), summary.submissionCount(),
                        open ? week.getAnnouncement() : null,
                        open ? week.getIntent() : null,
                        open ? week.getConstraints() : null),
                new Stats(submissions, participants, 0, participants == 0 ? 0 : Math.min(1, (double) submissions / participants)),
                null,
                Neighbor.from(prev, now),
                Neighbor.from(next, now));
    }
}
