package com.ttukttak.week.presentation;

import com.ttukttak.common.exception.ApiError;
import com.ttukttak.week.presentation.request.ImportWeeksRequest;
import com.ttukttak.week.presentation.request.UpdateWeekRequest;
import com.ttukttak.week.presentation.response.WeekDetailResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/** Swagger 문서만 모아둔 곳이에요. 매핑과 구현은 AdminWeekControllerImpl에 있어요. */
@Tag(name = "Admin", description = "운영자 전용 (role: admin)")
public interface AdminWeekController {

    @Operation(summary = "주차 가져오기", description = "weeks.json 형식을 그대로 받아요. id가 같으면 덮어쓰고, 하나라도 틀리면 모두 취소돼요.")
    @ApiResponse(responseCode = "204", description = "넣었어요")
    @ApiResponse(responseCode = "403", description = "forbidden",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "409", description = "conflict (회차가 겹침)",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "422", description = "validation_failed",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    void importWeeks(ImportWeeksRequest request);

    @Operation(summary = "주차 수정", description = "주제 문구, 일정, 상태. 보낸 필드만 바뀌어요. 공개된 주차의 일정은 force: true가 필요해요.")
    @ApiResponse(responseCode = "200", description = "고쳤어요")
    @ApiResponse(responseCode = "409", description = "conflict (force 없이 공개된 주차의 일정 변경)",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "422", description = "validation_failed",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    WeekDetailResponse update(String weekId, UpdateWeekRequest request);

    @Operation(summary = "주차 공지", description = "상태를 announced로 바꾸고 공개 시각에 구독자에게 주제 공개 메일을 보내요.")
    @ApiResponse(responseCode = "204", description = "예약했어요")
    @ApiResponse(responseCode = "409", description = "break_week",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    void announce(String weekId);
}
