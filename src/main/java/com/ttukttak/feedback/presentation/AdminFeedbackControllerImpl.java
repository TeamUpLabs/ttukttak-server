package com.ttukttak.feedback.presentation;

import com.ttukttak.feedback.application.FeedbackFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/feedback")
@RequiredArgsConstructor
public class AdminFeedbackControllerImpl implements AdminFeedbackController {
    private final FeedbackFacade feedbackFacade;

    @Override
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void hide(@PathVariable UUID id) {
        feedbackFacade.hide(id);
    }
}
