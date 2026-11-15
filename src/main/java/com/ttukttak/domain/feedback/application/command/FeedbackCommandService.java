package com.ttukttak.domain.feedback.application.command;

import com.ttukttak.domain.feedback.domain.Feedback;
import com.ttukttak.domain.feedback.domain.FeedbackRepository;
import com.ttukttak.domain.feedback.domain.FeedbackType;
import com.ttukttak.domain.feedback.exception.FeedbackForbiddenException;
import com.ttukttak.domain.feedback.exception.FeedbackNotFoundException;
import com.ttukttak.domain.submission.domain.Submission;
import com.ttukttak.domain.submission.domain.SubmissionRepository;
import com.ttukttak.domain.submission.exception.OwnSubmissionException;
import com.ttukttak.domain.submission.exception.SubmissionNotFoundException;
import com.ttukttak.domain.week.domain.Phase;
import com.ttukttak.domain.week.domain.WeekRepository;
import com.ttukttak.domain.week.exception.WeekNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

// ponytail: 요청 제한(작성 분당 5회·하루 100회, 도움돼요 분당 60회)은 아직 없어요.
@Service
@RequiredArgsConstructor
@Transactional
public class FeedbackCommandService {
    private final FeedbackRepository feedbackRepository;
    private final SubmissionRepository submissionRepository;
    private final WeekRepository weekRepository;

    /**
     * 언제든 남길 수 있지만 포인트는 제작·투표 기간에 남긴 것만 쌓여요. 내 제출작에는 못 남겨요.
     * 제출작 행을 공유 잠금으로 읽어서 철회와 동시에 와도 주인 없는 피드백이 남지 않아요.
     */
    public Feedback create(UUID submissionId, UUID userId, FeedbackType type, String body, Instant now) {
        Submission submission = submissionRepository.findByIdForShare(submissionId)
                .orElseThrow(SubmissionNotFoundException::new);
        if (submission.isAuthor(userId)) throw OwnSubmissionException.feedback();
        Phase phase = weekRepository.findById(submission.getWeekId()).orElseThrow(WeekNotFoundException::new).phase(now);
        boolean earnsPoints = phase == Phase.BUILDING || phase == Phase.VOTING;
        return feedbackRepository.save(Feedback.create(submissionId, userId, type, body, earnsPoints));
    }

    public Feedback edit(UUID id, UUID userId, FeedbackType type, String body, Instant now) {
        Feedback feedback = mine(id, userId);
        feedback.edit(type, body, now);
        return feedbackRepository.save(feedback);
    }

    /** 언제든 지울 수 있어요. 받은 포인트도 함께 사라져요. */
    public void delete(UUID id, UUID userId) {
        feedbackRepository.delete(mine(id, userId).getId());
    }

    /** 내 피드백에는 못 눌러요. 여러 번 불러도 한 번만 세요. 새 개수를 돌려줘요. */
    public long markHelpful(UUID id, UUID userId) {
        Feedback feedback = feedbackRepository.findByIdForShare(id).orElseThrow(FeedbackNotFoundException::new);
        if (feedback.isAuthor(userId)) throw FeedbackForbiddenException.ownFeedback();
        feedbackRepository.markHelpful(id, userId);
        return helpfulCount(id);
    }

    public long unmarkHelpful(UUID id, UUID userId) {
        feedbackRepository.findByIdForShare(id).orElseThrow(FeedbackNotFoundException::new);
        feedbackRepository.unmarkHelpful(id, userId);
        return helpfulCount(id);
    }

    /** 제출작을 만든 사람만. body가 null이면 답글을 지워요. */
    public Feedback reply(UUID id, UUID userId, String body, Instant now) {
        Feedback feedback = feedbackRepository.findByIdForUpdate(id).orElseThrow(FeedbackNotFoundException::new);
        Submission submission = submissionRepository.findById(feedback.getSubmissionId())
                .orElseThrow(FeedbackNotFoundException::new);
        if (!submission.isAuthor(userId)) throw FeedbackForbiddenException.notSubmissionAuthor();
        feedback.reply(body, now);
        return feedbackRepository.save(feedback);
    }

    // 수정·답글·삭제가 서로 덮어쓰지 않게 행을 잠가요.
    private Feedback mine(UUID id, UUID userId) {
        Feedback feedback = feedbackRepository.findByIdForUpdate(id).orElseThrow(FeedbackNotFoundException::new);
        if (!feedback.isAuthor(userId)) throw FeedbackForbiddenException.notAuthor();
        return feedback;
    }

    private long helpfulCount(UUID id) {
        return feedbackRepository.helpfulCounts(List.of(id)).getOrDefault(id, 0L);
    }
}
