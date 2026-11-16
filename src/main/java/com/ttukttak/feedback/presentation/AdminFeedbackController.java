package com.ttukttak.feedback.presentation;

import com.ttukttak.common.exception.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.UUID;

/** Swagger 문서만 모아둔 곳이에요. 매핑과 구현은 AdminFeedbackControllerImpl에 있어요. */
@Tag(name = "Admin", description = "운영자 전용 (role: admin)")
public interface AdminFeedbackController {

    @Operation(summary = "피드백 숨기기", description = "부적절한 피드백을 지우지 않고 목록과 포인트에서 빼요.")
    @ApiResponse(responseCode = "204", description = "숨겼어요")
    @ApiResponse(responseCode = "404", description = "not_found (없거나 이미 숨김)",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    void hide(UUID id);
}
