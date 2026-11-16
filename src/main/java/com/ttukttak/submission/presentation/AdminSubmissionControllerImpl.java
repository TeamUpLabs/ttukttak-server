package com.ttukttak.submission.presentation;

import com.ttukttak.submission.application.SubmissionFacade;
import com.ttukttak.submission.presentation.request.HideSubmissionRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/submissions")
@RequiredArgsConstructor
public class AdminSubmissionControllerImpl implements AdminSubmissionController {
    private final SubmissionFacade submissionFacade;

    @Override
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void hide(@PathVariable UUID id, @Valid @RequestBody HideSubmissionRequest request) {
        submissionFacade.hide(id, request.reason());
    }
}
