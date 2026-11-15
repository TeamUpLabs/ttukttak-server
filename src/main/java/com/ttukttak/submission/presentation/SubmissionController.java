package com.ttukttak.submission.presentation;

import com.ttukttak.common.exception.ApiError;
import com.ttukttak.domain.submission.domain.SubmissionSort;
import com.ttukttak.domain.submission.domain.Track;
import com.ttukttak.submission.presentation.request.SubmitRequest;
import com.ttukttak.submission.presentation.request.UpdateSubmissionRequest;
import com.ttukttak.submission.presentation.response.SubmissionDetailResponse;
import com.ttukttak.submission.presentation.response.SubmissionListResponse;
import com.ttukttak.submission.presentation.response.SubmitResponse;
import com.ttukttak.submission.presentation.response.VoteResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.security.Principal;
import java.util.UUID;

/** Swagger 문서만 모아둔 곳이에요. 매핑과 구현은 SubmissionControllerImpl에 있어요. */
@Tag(name = "Submission", description = "제출작")
public interface SubmissionController {

    @Operation(summary = "주차의 제출작", description = "sort가 없으면 제작 기간엔 random, 이후엔 votes예요.")
    @ApiResponse(responseCode = "200", description = "찾았어요")
    @ApiResponse(responseCode = "404", description = "not_found (주차 없음)",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    SubmissionListResponse list(@Parameter(description = "주차 id", example = "2026-w47") String weekId,
                                @Parameter(description = "beginner, regular, expert. 없으면 전체") Track track,
                                @Parameter(description = "votes, recent, random") SubmissionSort sort,
                                @Parameter(description = "빼고 볼 제출작 id") UUID exclude,
                                @Parameter(description = "이전 응답의 nextCursor") String cursor,
                                @Parameter(description = "기본 12, 최대 50") int limit,
                                Principal principal);

    @Operation(summary = "제출작 상세")
    @ApiResponse(responseCode = "200", description = "찾았어요")
    @ApiResponse(responseCode = "404", description = "not_found",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    SubmissionDetailResponse get(UUID id, Principal principal);

    @Operation(summary = "제출하기", description = "제작 기간에만, 한 주 한 번. 참가하기를 안 눌렀어도 참가로 쳐요.")
    @ApiResponse(responseCode = "201", description = "제출했어요")
    @ApiResponse(responseCode = "409", description = "already_submitted (submissionId 포함), submission_closed, break_week",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "422", description = "validation_failed",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    SubmitResponse submit(String weekId, SubmitRequest request, Principal principal);

    @Operation(summary = "제출작 수정", description = "보낸 필드만 바뀌어요. 본인만, 제출 마감 전까지만.")
    @ApiResponse(responseCode = "200", description = "고쳤어요")
    @ApiResponse(responseCode = "403", description = "forbidden",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "409", description = "submission_closed",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    SubmissionDetailResponse update(UUID id, UpdateSubmissionRequest request, Principal principal);

    @Operation(summary = "제출 철회", description = "본인만, 제출 마감 전까지만.")
    @ApiResponse(responseCode = "204", description = "철회했어요")
    @ApiResponse(responseCode = "403", description = "forbidden",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "409", description = "submission_closed",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    void withdraw(UUID id, Principal principal);

    @Operation(summary = "투표", description = "투표 기간에만. 여러 번 불러도 한 표만 세요.")
    @ApiResponse(responseCode = "200", description = "투표했어요")
    @ApiResponse(responseCode = "403", description = "own_submission",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "409", description = "voting_closed",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    VoteResponse vote(UUID id, Principal principal);

    @Operation(summary = "투표 취소", description = "투표 기간에만.")
    @ApiResponse(responseCode = "200", description = "취소했어요")
    @ApiResponse(responseCode = "409", description = "voting_closed",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    VoteResponse unvote(UUID id, Principal principal);
}
