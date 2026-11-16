package com.ttukttak.profile.presentation;

import com.ttukttak.common.exception.ApiError;
import com.ttukttak.profile.presentation.response.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.security.Principal;

/** Swagger 문서만 모아둔 곳이에요. 매핑과 구현은 ProfileControllerImpl에 있어요. */
@Tag(name = "User", description = "사용자 프로필")
public interface ProfileController {

    @Operation(summary = "프로필 조회")
    @ApiResponse(responseCode = "200", description = "찾았어요")
    @ApiResponse(responseCode = "404", description = "not_found",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    ProfileResponse get(@Parameter(description = "@ 없는 아이디", example = "jh.park") String handle, Principal principal);

    @Operation(summary = "52주 기록 격자", description = "오래된 주부터 이번 주까지. 주차가 52개보다 적으면 있는 만큼만.")
    @ApiResponse(responseCode = "200", description = "찾았어요")
    StreakResponse streak(String handle);

    @Operation(summary = "배지", description = "연속 제출, 트랙 TOP 3, 남긴 피드백. 받은 순서대로.")
    @ApiResponse(responseCode = "200", description = "찾았어요")
    BadgeListResponse badges(String handle);

    @Operation(summary = "제출작 탭", description = "최신순. 결과 발표 뒤 트랙 3위 안이면 medal이 붙어요.")
    @ApiResponse(responseCode = "200", description = "찾았어요")
    ProfileSubmissionListResponse submissions(String handle,
                                              @Parameter(description = "이전 응답의 nextCursor") String cursor,
                                              @Parameter(description = "기본 12, 최대 50") int limit,
                                              Principal principal);

    @Operation(summary = "피드백 활동 탭", description = "최신순")
    @ApiResponse(responseCode = "200", description = "찾았어요")
    ProfileFeedbackListResponse feedback(String handle,
                                         @Parameter(description = "이전 응답의 nextCursor") String cursor,
                                         @Parameter(description = "기본 20, 최대 50") int limit);

    @Operation(summary = "팔로우", description = "여러 번 불러도 한 번만 세요. 나 자신은 못 해요.")
    @ApiResponse(responseCode = "200", description = "팔로우했어요")
    @ApiResponse(responseCode = "403", description = "forbidden (나 자신)",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    FollowResponse follow(String handle, Principal principal);

    @Operation(summary = "팔로우 취소")
    @ApiResponse(responseCode = "200", description = "취소했어요")
    FollowResponse unfollow(String handle, Principal principal);
}
