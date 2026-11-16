package com.ttukttak.feedback.application;

import com.ttukttak.common.OffsetCursor;
import com.ttukttak.domain.feedback.application.command.FeedbackCommandService;
import com.ttukttak.domain.feedback.application.query.FeedbackQueryService;
import com.ttukttak.domain.feedback.domain.Feedback;
import com.ttukttak.domain.feedback.domain.FeedbackSort;
import com.ttukttak.domain.feedback.domain.FeedbackType;
import com.ttukttak.domain.feedback.exception.FeedbackNotFoundException;
import com.ttukttak.domain.submission.application.query.SubmissionQueryService;
import com.ttukttak.domain.user.application.query.UserQueryService;
import com.ttukttak.domain.user.domain.User;
import com.ttukttak.feedback.presentation.request.CreateFeedbackRequest;
import com.ttukttak.feedback.presentation.request.ReplyRequest;
import com.ttukttak.feedback.presentation.request.UpdateFeedbackRequest;
import com.ttukttak.feedback.presentation.response.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

/** 요청·응답 DTO와 도메인 사이를 이어줘요. 트랜잭션은 각 서비스가 가져요. */
@Component
@RequiredArgsConstructor
public class FeedbackFacade {
    private final FeedbackCommandService feedbackCommandService;
    private final FeedbackQueryService feedbackQueryService;
    private final SubmissionQueryService submissionQueryService;
    private final UserQueryService userQueryService;

    public FeedbackListResponse list(UUID submissionId, FeedbackType type, FeedbackSort sort, String cursor, int limit,
                                     UUID viewerId) {
        submissionQueryService.get(submissionId); // 없거나 철회했으면 404
        OffsetCursor at = OffsetCursor.decode(cursor);
        OffsetCursor.Slice<Feedback> page = at.slice(limit, n -> feedbackQueryService.page(submissionId, type,
                sort == null ? FeedbackSort.HELPFUL : sort, at.offset(), n));
        return new FeedbackListResponse(items(page.items(), viewerId),
                FeedbackListResponse.Counts.from(feedbackQueryService.countByType(submissionId)), page.next());
    }

    public CreateFeedbackResponse create(UUID submissionId, UUID userId, CreateFeedbackRequest request) {
        long before = feedbackQueryService.points(userId);
        Feedback saved = feedbackCommandService.create(submissionId, userId, request.type(), request.body(), Instant.now());
        long after = feedbackQueryService.points(userId);
        return new CreateFeedbackResponse(item(saved, userId), after - before, after);
    }

    public FeedbackResponse edit(UUID id, UUID userId, UpdateFeedbackRequest request) {
        return new FeedbackResponse(item(feedbackCommandService.edit(id, userId, request.type(), request.body(),
                Instant.now()), userId));
    }

    public void delete(UUID id, UUID userId) {
        feedbackCommandService.delete(id, userId);
    }

    public void hide(UUID id) {
        feedbackCommandService.hide(id);
    }

    public HelpfulResponse markHelpful(UUID id, UUID userId) {
        return new HelpfulResponse(true, feedbackCommandService.markHelpful(id, userId));
    }

    public HelpfulResponse unmarkHelpful(UUID id, UUID userId) {
        return new HelpfulResponse(false, feedbackCommandService.unmarkHelpful(id, userId));
    }

    public FeedbackResponse reply(UUID id, UUID userId, ReplyRequest request) {
        return new FeedbackResponse(item(feedbackCommandService.reply(id, userId, request.body(), Instant.now()), userId));
    }

    public FeedbackResponse deleteReply(UUID id, UUID userId) {
        return new FeedbackResponse(item(feedbackCommandService.reply(id, userId, null, Instant.now()), userId));
    }

    private FeedbackItem item(Feedback feedback, UUID viewerId) {
        return items(List.of(feedback), viewerId).stream().findFirst().orElseThrow(FeedbackNotFoundException::new);
    }

    // 작성자가 탈퇴했으면 빼요.
    private List<FeedbackItem> items(List<Feedback> feedback, UUID viewerId) {
        List<UUID> ids = feedback.stream().map(Feedback::getId).toList();
        Map<UUID, User> authors = userQueryService.getAll(feedback.stream().map(Feedback::getUserId).toList());
        Map<UUID, Long> helpfulCounts = feedbackQueryService.helpfulCounts(ids);
        Set<UUID> helpful = feedbackQueryService.helpful(viewerId, ids);
        return feedback.stream().filter(f -> authors.containsKey(f.getUserId()))
                .map(f -> FeedbackItem.of(f, authors.get(f.getUserId()), helpfulCounts.getOrDefault(f.getId(), 0L),
                        viewerId, helpful.contains(f.getId())))
                .toList();
    }
}
