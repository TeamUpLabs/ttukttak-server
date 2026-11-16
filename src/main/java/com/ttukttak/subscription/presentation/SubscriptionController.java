package com.ttukttak.subscription.presentation;

import com.ttukttak.common.exception.ApiError;
import com.ttukttak.subscription.presentation.request.SubscribeRequest;
import com.ttukttak.subscription.presentation.response.SubscribeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

/** Swagger 문서만 모아둔 곳이에요. 매핑과 구현은 SubscriptionControllerImpl에 있어요. */
@Tag(name = "Subscription", description = "주제 공개 알림")
public interface SubscriptionController {

    @Operation(summary = "알림 신청", description = "확인 메일의 링크를 눌러야 활성화돼요. 이미 신청한 주소여도 같은 응답이에요.")
    @ApiResponse(responseCode = "202", description = "확인 메일을 보냈어요")
    @ApiResponse(responseCode = "422", description = "validation_failed",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    SubscribeResponse subscribe(SubscribeRequest request);

    @Operation(summary = "신청 확인 링크", description = "성공하면 /?subscribed=1, 틀린 링크면 /?subscribed=0으로 302")
    @ApiResponse(responseCode = "302", description = "메인으로 이동")
    ResponseEntity<Void> confirm(String token);

    @Operation(summary = "구독 해지 링크", description = "성공하면 /?unsubscribed=1, 틀린 링크면 /?unsubscribed=0으로 302")
    @ApiResponse(responseCode = "302", description = "메인으로 이동")
    ResponseEntity<Void> unsubscribe(String token);
}
