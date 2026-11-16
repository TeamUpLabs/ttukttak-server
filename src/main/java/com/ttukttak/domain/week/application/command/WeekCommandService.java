package com.ttukttak.domain.week.application.command;

import com.ttukttak.domain.submission.domain.SubmissionRepository;
import com.ttukttak.domain.submission.exception.AlreadySubmittedException;
import com.ttukttak.domain.week.domain.*;
import com.ttukttak.domain.week.exception.ScheduleLockedException;
import com.ttukttak.domain.week.exception.WeekNotFoundException;
import com.ttukttak.domain.week.exception.WeekPhaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.UnaryOperator;

@Service
@RequiredArgsConstructor
@Transactional
public class WeekCommandService {
    private final WeekRepository weekRepository;
    private final ParticipationRepository participationRepository;
    private final SubmissionRepository submissionRepository;

    /** id가 같으면 덮어써요. 버튼 누른 횟수는 덮어쓰지 않아요. 하나라도 틀리면 모두 취소돼요. */
    public void importWeeks(List<Week> weeks) {
        for (Week week : weeks) {
            week.validate();
            weekRepository.save(week);
        }
    }

    /** 공개된 주차의 일정은 force 없이 못 바꿔요. 바꾼 뒤에도 일정 규칙을 지켜야 해요. */
    public Week update(String id, UnaryOperator<Week> patch, boolean force, Instant now) {
        Week week = weekRepository.findById(id).orElseThrow(WeekNotFoundException::new);
        Week patched = patch.apply(week);
        if (week.isOpen(now) && !week.sameSchedule(patched) && !force) throw new ScheduleLockedException();
        patched.validate();
        return weekRepository.save(patched);
    }

    /** 상태를 공지 완료로 바꾸고, 공개 시각에 주제 공개 메일이 나가게 해요. 이미 열렸으면 다음 예약 작업 때 바로 나가요. */
    public void announce(String id) {
        Week week = weekRepository.findById(id).orElseThrow(WeekNotFoundException::new);
        if (week.getType() == WeekType.BREAK) throw WeekPhaseException.breakWeek();
        weekRepository.save(week.toBuilder().status(WeekStatus.ANNOUNCED).build());
        weekRepository.requestAnnounceMail(id);
    }

    public boolean claimAnnounceMail(String id) {
        return weekRepository.claimAnnounceMail(id);
    }

    // ponytail: 요청 제한(세션당 초당 5회)은 아직 없어요. 장난이 심해지면 붙여요.
    public long press(int count, Instant now) {
        return weekRepository.addPresses(current(now).getId(), count);
    }

    public long join(UUID userId, Instant now) {
        Week week = building(now);
        participationRepository.join(week.getId(), userId);
        return participationRepository.count(week.getId());
    }

    /**
     * 이미 제출했으면 취소할 수 없어요. 먼저 지우고(행 잠금) 나서 제출을 확인해요.
     * 제출이 동시에 들어와도 둘 중 하나가 참가 행에서 기다리니 "제출했는데 참가 안 함"이 남지 않아요.
     */
    public long leave(UUID userId, Instant now) {
        Week week = building(now);
        participationRepository.leave(week.getId(), userId);
        submissionRepository.findByWeekIdAndUserId(week.getId(), userId).ifPresent(submission -> {
            throw new AlreadySubmittedException(submission.getId());
        });
        return participationRepository.count(week.getId());
    }

    private Week building(Instant now) {
        Week week = current(now);
        week.requireBuilding(now);
        return week;
    }

    private Week current(Instant now) {
        return weekRepository.findLatestOpened(now)
                .filter(week -> week.phase(now) != Phase.CLOSED)
                .orElseThrow(WeekNotFoundException::new);
    }
}
