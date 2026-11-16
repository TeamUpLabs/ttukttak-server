package com.ttukttak.suggestion.presentation;

import com.ttukttak.common.exception.ApiError;
import com.ttukttak.suggestion.presentation.request.SuggestThemeRequest;
import com.ttukttak.suggestion.presentation.response.ThemeSuggestionListResponse;
import com.ttukttak.suggestion.presentation.response.ThemeSuggestionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.security.Principal;

/** Swagger 문서만 모아둔 곳이에요. 매핑과 구현은 ThemeSuggestionControllerImpl에 있어요. */
@Tag(name = "ThemeSuggestion", description = "주제 제안")
public interface ThemeSuggestionController {

    @Operation(summary = "주제 제안하기", description = "시즌 후반 주제 투표 후보로 써요.")
    @ApiResponse(responseCode = "201", description = "제안했어요")
    @ApiResponse(responseCode = "422", description = "validation_failed",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    ThemeSuggestionResponse suggest(SuggestThemeRequest request, Principal principal);

    @Operation(summary = "제안 목록 (운영자)", description = "최신순")
    @ApiResponse(responseCode = "200", description = "찾았어요")
    ThemeSuggestionListResponse list(@Parameter(description = "이전 응답의 nextCursor") String cursor,
                                     @Parameter(description = "기본 20, 최대 50") int limit);
}
