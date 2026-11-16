package com.ttukttak.subscription.presentation.response;

/** 이미 신청한 주소여도 같은 응답이라 가입 여부가 드러나지 않아요. */
public record SubscribeResponse(String status) {
    public static final SubscribeResponse PENDING = new SubscribeResponse("pending");
}
