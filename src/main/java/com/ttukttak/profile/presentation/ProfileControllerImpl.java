package com.ttukttak.profile.presentation;

import com.ttukttak.profile.application.ProfileFacade;
import com.ttukttak.profile.presentation.response.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/{handle}")
@RequiredArgsConstructor
public class ProfileControllerImpl implements ProfileController {
    private final ProfileFacade profileFacade;

    @Override
    @GetMapping
    public ProfileResponse get(@PathVariable String handle, Principal principal) {
        return profileFacade.get(handle, userId(principal));
    }

    @Override
    @GetMapping("/streak")
    public StreakResponse streak(@PathVariable String handle) {
        return profileFacade.streak(handle);
    }

    @Override
    @GetMapping("/badges")
    public BadgeListResponse badges(@PathVariable String handle) {
        return profileFacade.badges(handle);
    }

    @Override
    @GetMapping("/submissions")
    public ProfileSubmissionListResponse submissions(@PathVariable String handle,
                                                     @RequestParam(required = false) String cursor,
                                                     @RequestParam(defaultValue = "12") int limit,
                                                     Principal principal) {
        return profileFacade.submissions(handle, cursor, limit, userId(principal));
    }

    @Override
    @GetMapping("/feedback")
    public ProfileFeedbackListResponse feedback(@PathVariable String handle,
                                                @RequestParam(required = false) String cursor,
                                                @RequestParam(defaultValue = "20") int limit) {
        return profileFacade.feedback(handle, cursor, limit);
    }

    @Override
    @PutMapping("/follow")
    public FollowResponse follow(@PathVariable String handle, Principal principal) {
        return profileFacade.follow(handle, userId(principal));
    }

    @Override
    @DeleteMapping("/follow")
    public FollowResponse unfollow(@PathVariable String handle, Principal principal) {
        return profileFacade.unfollow(handle, userId(principal));
    }

    // 공개 API는 비로그인이면 principal이 null이에요.
    private static UUID userId(Principal principal) {
        return principal == null ? null : UUID.fromString(principal.getName());
    }
}
