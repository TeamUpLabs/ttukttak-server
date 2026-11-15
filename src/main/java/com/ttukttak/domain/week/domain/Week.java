package com.ttukttak.domain.week.domain;

import com.ttukttak.domain.week.exception.InvalidWeekException;
import com.ttukttak.domain.week.exception.WeekPhaseException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static lombok.AccessLevel.PRIVATE;

@Getter
@Builder
@AllArgsConstructor(access = PRIVATE)
public class Week {
    private String id;               // "2026-w47"
    private WeekType type;
    private Integer round;           // 전체 통틀어 몇 번째 주제인지. 휴식 주는 null
    private String code;             // "W47"
    private String title;
    private String announcement;
    private String intent;
    private String rule;
    private List<String> constraints;
    private Difficulty difficulty;
    private WeekStatus status;
    private Examples examples;
    private Instant opensAt;
    private Instant submissionClosesAt;
    private Instant votingClosesAt;
    private Instant resultsAt;
    private String notes;
    private long presses;

    public Phase phase(Instant now) {
        if (type == WeekType.BREAK) return Phase.BREAK;
        if (now.isBefore(opensAt)) return Phase.UPCOMING;
        if (now.isBefore(submissionClosesAt)) return Phase.BUILDING;
        if (now.isBefore(votingClosesAt)) return Phase.VOTING;
        if (now.isBefore(resultsAt)) return Phase.TALLYING;
        return Phase.CLOSED;
    }

    /** 공개 전엔 주제를 숨겨요. */
    public boolean isOpen(Instant now) {
        return !now.isBefore(opensAt);
    }

    /** 참가·제출은 제작 기간에만 바꿀 수 있어요. */
    public void requireBuilding(Instant now) {
        Phase phase = phase(now);
        if (phase == Phase.BREAK) throw WeekPhaseException.breakWeek();
        if (phase != Phase.BUILDING) throw WeekPhaseException.submissionClosed();
    }

    /** 투표는 제출 마감부터 투표 마감까지만 받아요. */
    public void requireVoting(Instant now) {
        Phase phase = phase(now);
        if (phase == Phase.BREAK) throw WeekPhaseException.breakWeek();
        if (phase != Phase.VOTING) throw WeekPhaseException.votingClosed();
    }

    /** 투표가 끝나야 순위를 보여줘요. */
    public boolean isVotingOver(Instant now) {
        Phase phase = phase(now);
        return phase == Phase.TALLYING || phase == Phase.CLOSED;
    }

    /** 주제 주는 일정 4개가 순서대로 있어야 하고, 휴식 주는 회차·마감이 없어요. */
    public void validate() {
        if (type == WeekType.THEME) {
            boolean ordered = round != null
                    && Stream.of(submissionClosesAt, votingClosesAt, resultsAt).allMatch(Objects::nonNull)
                    && opensAt.isBefore(submissionClosesAt)
                    && submissionClosesAt.isBefore(votingClosesAt)
                    && !resultsAt.isBefore(votingClosesAt);
            if (!ordered) throw new InvalidWeekException(id + ": 주제 주는 회차와 일정 4개가 순서대로 있어야 해요.");
        } else if (round != null || submissionClosesAt != null || votingClosesAt != null || resultsAt != null) {
            throw new InvalidWeekException(id + ": 휴식 주는 회차와 마감 일정이 없어야 해요.");
        }
    }
}
