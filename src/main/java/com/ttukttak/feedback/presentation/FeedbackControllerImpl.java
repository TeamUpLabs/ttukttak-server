package com.ttukttak.feedback.presentation;

import com.ttukttak.domain.feedback.domain.FeedbackSort;
import com.ttukttak.domain.feedback.domain.FeedbackType;
import com.ttukttak.feedback.application.FeedbackFacade;
import com.ttukttak.feedback.presentation.request.CreateFeedbackRequest;
import com.ttukttak.feedback.presentation.request.ReplyRequest;
import com.ttukttak.feedback.presentation.request.UpdateFeedbackRequest;
import com.ttukttak.feedback.presentation.response.CreateFeedbackResponse;
import com.ttukttak.feedback.presentation.response.FeedbackListResponse;
import com.ttukttak.feedback.presentation.response.FeedbackResponse;
import com.ttukttak.feedback.presentation.response.HelpfulResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class FeedbackControllerImpl implements FeedbackController {
    private final FeedbackFacade feedbackFacade;

    @Override
    @GetMapping("/submissions/{submissionId}/feedback")
    public FeedbackListResponse list(@PathVariable UUID submissionId,
                                     @RequestParam(required = false) FeedbackType type,
                                     @RequestParam(required = false) FeedbackSort sort,
                                     @RequestParam(required = false) String cursor,
                                     @RequestParam(defaultValue = "20") int limit,
                                     Principal principal) {
        return feedbackFacade.list(submissionId, type, sort, cursor, limit, userId(principal));
    }

    @Override
    @PostMapping("/submissions/{submissionId}/feedback")
    @ResponseStatus(HttpStatus.CREATED)
    public CreateFeedbackResponse create(@PathVariable UUID submissionId, @Valid @RequestBody CreateFeedbackRequest request,
                                         Principal principal) {
        return feedbackFacade.create(submissionId, userId(principal), request);
    }

    @Override
    @PatchMapping("/feedback/{id}")
    public FeedbackResponse edit(@PathVariable UUID id, @Valid @RequestBody UpdateFeedbackRequest request,
                                 Principal principal) {
        return feedbackFacade.edit(id, userId(principal), request);
    }

    @Override
    @DeleteMapping("/feedback/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id, Principal principal) {
        feedbackFacade.delete(id, userId(principal));
    }

    @Override
    @PutMapping("/feedback/{id}/helpful")
    public HelpfulResponse markHelpful(@PathVariable UUID id, Principal principal) {
        return feedbackFacade.markHelpful(id, userId(principal));
    }

    @Override
    @DeleteMapping("/feedback/{id}/helpful")
    public HelpfulResponse unmarkHelpful(@PathVariable UUID id, Principal principal) {
        return feedbackFacade.unmarkHelpful(id, userId(principal));
    }

    @Override
    @PutMapping("/feedback/{id}/reply")
    public FeedbackResponse reply(@PathVariable UUID id, @Valid @RequestBody ReplyRequest request, Principal principal) {
        return feedbackFacade.reply(id, userId(principal), request);
    }

    @Override
    @DeleteMapping("/feedback/{id}/reply")
    public FeedbackResponse deleteReply(@PathVariable UUID id, Principal principal) {
        return feedbackFacade.deleteReply(id, userId(principal));
    }

    // 공개 API는 비로그인이면 principal이 null이에요.
    private static UUID userId(Principal principal) {
        return principal == null ? null : UUID.fromString(principal.getName());
    }
}
