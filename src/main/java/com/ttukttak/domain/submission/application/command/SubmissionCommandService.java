package com.ttukttak.domain.submission.application.command;

import com.ttukttak.domain.submission.domain.Submission;
import com.ttukttak.domain.submission.domain.SubmissionContent;
import com.ttukttak.domain.submission.domain.SubmissionRepository;
import com.ttukttak.domain.submission.exception.AlreadySubmittedException;
import com.ttukttak.domain.submission.exception.NotAuthorException;
import com.ttukttak.domain.submission.exception.SubmissionNotFoundException;
import com.ttukttak.domain.week.domain.ParticipationRepository;
import com.ttukttak.domain.week.domain.WeekRepository;
import com.ttukttak.domain.week.exception.WeekNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

// ponytail: 요청 제한(사용자당 분당 10회)은 아직 없어요.
@Service
@RequiredArgsConstructor
@Transactional
public class SubmissionCommandService {
    private final SubmissionRepository submissionRepository;
    private final WeekRepository weekRepository;
    private final ParticipationRepository participationRepository;

    /** 제작 기간에 한 주 한 번. 참가하기를 안 눌렀어도 제출하면 참가로 쳐요. */
    public Submission submit(String weekId, UUID userId, SubmissionContent content, Instant now) {
        requireBuilding(weekId, now);
        submissionRepository.findByWeekIdAndUserId(weekId, userId).ifPresent(existing -> {
            throw new AlreadySubmittedException(existing.getId());
        });
        Submission saved = submissionRepository.save(Submission.create(weekId, userId, content));
        participationRepository.join(weekId, userId);
        return saved;
    }

    /** 보낸 칸만 바꿔요. */
    public Submission update(UUID id, UUID userId, SubmissionContent patch, Instant now) {
        Submission submission = editable(id, userId, now);
        submission.update(patch);
        return submissionRepository.save(submission);
    }

    public void withdraw(UUID id, UUID userId, Instant now) {
        submissionRepository.delete(editable(id, userId, now).getId());
    }

    // 본인 것만, 제출 마감 전까지만 고칠 수 있어요.
    private Submission editable(UUID id, UUID userId, Instant now) {
        Submission submission = submissionRepository.findById(id).orElseThrow(SubmissionNotFoundException::new);
        if (!submission.isAuthor(userId)) throw new NotAuthorException();
        requireBuilding(submission.getWeekId(), now);
        return submission;
    }

    private void requireBuilding(String weekId, Instant now) {
        weekRepository.findById(weekId).orElseThrow(WeekNotFoundException::new).requireBuilding(now);
    }
}
