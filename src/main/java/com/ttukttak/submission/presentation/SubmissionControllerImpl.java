package com.ttukttak.submission.presentation;

import com.ttukttak.domain.submission.domain.SubmissionSort;
import com.ttukttak.domain.submission.domain.Track;
import com.ttukttak.submission.application.SubmissionFacade;
import com.ttukttak.submission.presentation.request.SubmitRequest;
import com.ttukttak.submission.presentation.request.UpdateSubmissionRequest;
import com.ttukttak.submission.presentation.response.SubmissionDetailResponse;
import com.ttukttak.submission.presentation.response.SubmissionListResponse;
import com.ttukttak.submission.presentation.response.SubmitResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SubmissionControllerImpl implements SubmissionController {
    private final SubmissionFacade submissionFacade;

    @Override
    @GetMapping("/weeks/{weekId}/submissions")
    public SubmissionListResponse list(@PathVariable String weekId,
                                       @RequestParam(required = false) Track track,
                                       @RequestParam(required = false) SubmissionSort sort,
                                       @RequestParam(required = false) UUID exclude,
                                       @RequestParam(required = false) String cursor,
                                       @RequestParam(defaultValue = "12") int limit,
                                       Principal principal) {
        return submissionFacade.list(weekId, track, sort, exclude, cursor, limit, userId(principal));
    }

    @Override
    @GetMapping("/submissions/{id}")
    public SubmissionDetailResponse get(@PathVariable UUID id, Principal principal) {
        return submissionFacade.get(id, userId(principal));
    }

    @Override
    @PostMapping("/weeks/{weekId}/submissions")
    @ResponseStatus(HttpStatus.CREATED)
    public SubmitResponse submit(@PathVariable String weekId, @Valid @RequestBody SubmitRequest request,
                                 Principal principal) {
        return submissionFacade.submit(weekId, userId(principal), request);
    }

    @Override
    @PatchMapping("/submissions/{id}")
    public SubmissionDetailResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateSubmissionRequest request,
                                           Principal principal) {
        return submissionFacade.update(id, userId(principal), request);
    }

    @Override
    @DeleteMapping("/submissions/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void withdraw(@PathVariable UUID id, Principal principal) {
        submissionFacade.withdraw(id, userId(principal));
    }

    // 공개 API는 비로그인이면 principal이 null이에요.
    private static UUID userId(Principal principal) {
        return principal == null ? null : UUID.fromString(principal.getName());
    }
}
