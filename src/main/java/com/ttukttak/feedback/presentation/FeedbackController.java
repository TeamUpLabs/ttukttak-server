package com.ttukttak.feedback.presentation;

import com.ttukttak.common.exception.ApiError;
import com.ttukttak.domain.feedback.domain.FeedbackSort;
import com.ttukttak.domain.feedback.domain.FeedbackType;
import com.ttukttak.feedback.presentation.request.CreateFeedbackRequest;
import com.ttukttak.feedback.presentation.request.ReplyRequest;
import com.ttukttak.feedback.presentation.request.UpdateFeedbackRequest;
import com.ttukttak.feedback.presentation.response.CreateFeedbackResponse;
import com.ttukttak.feedback.presentation.response.FeedbackListResponse;
import com.ttukttak.feedback.presentation.response.FeedbackResponse;
import com.ttukttak.feedback.presentation.response.HelpfulResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.security.Principal;
import java.util.UUID;

/** Swagger 문서만 모아둔 곳이에요. 매핑과 구현은 FeedbackControllerImpl에 있어요. */
@Tag(name = "Feedback", description = "피드백")
public interface FeedbackController {

    @Operation(summary = "제출작의 피드백", description = "counts로 필터 탭의 숫자를 그려요.")
    @ApiResponse(responseCode = "200", description = "찾았어요")
    @ApiResponse(responseCode = "404", description = "not_found (제출작 없음)",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    FeedbackListResponse list(UUID submissionId,
                              @Parameter(description = "bug, ux, code, kudo. 없으면 전체") FeedbackType type,
                              @Parameter(description = "helpful(기본), recent") FeedbackSort sort,
                              @Parameter(description = "이전 응답의 nextCursor") String cursor,
                              @Parameter(description = "기본 20, 최대 50") int limit,
                              Principal principal);

    @Operation(summary = "피드백 남기기",
            description = "언제든 남길 수 있지만 포인트는 제작·투표 기간에 남긴 것만 쌓여요(1개 10점, 주차마다 최대 100점).")
    @ApiResponse(responseCode = "201", description = "남겼어요")
    @ApiResponse(responseCode = "403", description = "own_submission",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "422", description = "validation_failed",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    CreateFeedbackResponse create(UUID submissionId, CreateFeedbackRequest request, Principal principal);

    @Operation(summary = "피드백 수정", description = "본인만, 작성 후 24시간 안에만. 보낸 필드만 바뀌어요.")
    @ApiResponse(responseCode = "200", description = "고쳤어요")
    @ApiResponse(responseCode = "403", description = "forbidden",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    FeedbackResponse edit(UUID id, UpdateFeedbackRequest request, Principal principal);

    @Operation(summary = "피드백 삭제", description = "본인만, 언제든. 받은 포인트도 사라져요.")
    @ApiResponse(responseCode = "204", description = "지웠어요")
    @ApiResponse(responseCode = "403", description = "forbidden",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    void delete(UUID id, Principal principal);

    @Operation(summary = "도움돼요", description = "내 피드백에는 못 눌러요. 여러 번 불러도 한 번만 세요.")
    @ApiResponse(responseCode = "200", description = "눌렀어요")
    @ApiResponse(responseCode = "403", description = "forbidden",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    HelpfulResponse markHelpful(UUID id, Principal principal);

    @Operation(summary = "도움돼요 취소")
    @ApiResponse(responseCode = "200", description = "취소했어요")
    HelpfulResponse unmarkHelpful(UUID id, Principal principal);

    @Operation(summary = "답글", description = "제출작을 만든 사람만. 하나만 있어서 덮어써요.")
    @ApiResponse(responseCode = "200", description = "달았어요")
    @ApiResponse(responseCode = "403", description = "forbidden",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    FeedbackResponse reply(UUID id, ReplyRequest request, Principal principal);

    @Operation(summary = "답글 삭제", description = "제출작을 만든 사람만.")
    @ApiResponse(responseCode = "200", description = "지웠어요")
    @ApiResponse(responseCode = "403", description = "forbidden",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    FeedbackResponse deleteReply(UUID id, Principal principal);
}
