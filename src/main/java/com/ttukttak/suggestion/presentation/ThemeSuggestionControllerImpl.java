package com.ttukttak.suggestion.presentation;

import com.ttukttak.suggestion.application.ThemeSuggestionFacade;
import com.ttukttak.suggestion.presentation.request.SuggestThemeRequest;
import com.ttukttak.suggestion.presentation.response.ThemeSuggestionListResponse;
import com.ttukttak.suggestion.presentation.response.ThemeSuggestionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ThemeSuggestionControllerImpl implements ThemeSuggestionController {
    private final ThemeSuggestionFacade themeSuggestionFacade;

    @Override
    @PostMapping("/theme-suggestions")
    @ResponseStatus(HttpStatus.CREATED)
    public ThemeSuggestionResponse suggest(@Valid @RequestBody SuggestThemeRequest request, Principal principal) {
        return themeSuggestionFacade.suggest(UUID.fromString(principal.getName()), request);
    }

    @Override
    @GetMapping("/admin/theme-suggestions")
    public ThemeSuggestionListResponse list(@RequestParam(required = false) String cursor,
                                            @RequestParam(defaultValue = "20") int limit) {
        return themeSuggestionFacade.list(cursor, limit);
    }
}
