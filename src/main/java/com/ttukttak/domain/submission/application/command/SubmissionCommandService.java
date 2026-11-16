package com.ttukttak.domain.submission.application.command;

import com.ttukttak.domain.feedback.domain.FeedbackRepository;
import com.ttukttak.domain.submission.domain.Submission;
import com.ttukttak.domain.submission.domain.SubmissionContent;
import com.ttukttak.domain.submission.domain.SubmissionRepository;
import com.ttukttak.domain.submission.domain.VoteRepository;
import com.ttukttak.domain.submission.exception.AlreadySubmittedException;
import com.ttukttak.domain.submission.exception.NotAuthorException;
import com.ttukttak.domain.submission.exception.OwnSubmissionException;
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
    private final VoteRepository voteRepository;
    private final FeedbackRepository feedbackRepository;

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

    /** 받은 투표와 피드백도 함께 지워요. */
    public void withdraw(UUID id, UUID userId, Instant now) {
        editable(id, userId, now);
        voteRepository.deleteBySubmissionId(id);
        feedbackRepository.deleteBySubmissionId(id);
        submissionRepository.delete(id);
    }

    /** 운영자 숨김. 지우지 않고 모든 공개 화면에서 빼요. */
    // ponytail: 명세는 작성자에게 메일을 보내지만, GitHub 로그인이라 메일 주소를 받지 않아서 아직 못 보내요.
    public void hide(UUID id, String reason) {
        if (!submissionRepository.hide(id, reason)) throw new SubmissionNotFoundException();
    }

    /** 투표 기간에만, 내 제출작은 안 돼요. 여러 번 불러도 한 표만 세요. 새 표 수를 돌려줘요. */
    // ponytail: 요청 제한(사용자당 분당 60회)은 아직 없어요.
    public long vote(UUID id, UUID userId, Instant now) {
        Submission submission = votable(id, now);
        if (submission.isAuthor(userId)) throw OwnSubmissionException.vote();
        voteRepository.vote(id, userId);
        return voteRepository.count(id);
    }

    public long unvote(UUID id, UUID userId, Instant now) {
        votable(id, now);
        voteRepository.unvote(id, userId);
        return voteRepository.count(id);
    }

    // 철회와 동시에 와도 표가 주인 없이 남지 않게 행을 공유 잠금으로 읽어요.
    private Submission votable(UUID id, Instant now) {
        Submission submission = submissionRepository.findByIdForShare(id).orElseThrow(SubmissionNotFoundException::new);
        weekRepository.findById(submission.getWeekId()).orElseThrow(WeekNotFoundException::new).requireVoting(now);
        return submission;
    }

    // 본인 것만, 제출 마감 전까지만 고칠 수 있어요. 동시 수정은 행 잠금으로 줄 세워요.
    private Submission editable(UUID id, UUID userId, Instant now) {
        Submission submission = submissionRepository.findByIdForUpdate(id).orElseThrow(SubmissionNotFoundException::new);
        if (!submission.isAuthor(userId)) throw new NotAuthorException();
        requireBuilding(submission.getWeekId(), now);
        return submission;
    }

    private void requireBuilding(String weekId, Instant now) {
        weekRepository.findById(weekId).orElseThrow(WeekNotFoundException::new).requireBuilding(now);
    }
}
