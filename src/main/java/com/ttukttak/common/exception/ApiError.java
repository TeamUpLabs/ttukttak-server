package com.ttukttak.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;
import java.util.UUID;

/** docs/api/README.md의 오류 형식: { "error": { "code", "message", "fields"?, "submissionId"? } } */
public record ApiError(Error error) {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Error(String code, String message, Map<String, String> fields, UUID submissionId) {
    }

    public static ApiError of(String code, String message) {
        return of(code, message, null);
    }

    public static ApiError of(String code, String message, Map<String, String> fields) {
        return new ApiError(new Error(code, message, fields, null));
    }

    /** already_submitted: 이미 낸 제출작 id를 함께 줘요. */
    public static ApiError alreadySubmitted(String message, UUID submissionId) {
        return new ApiError(new Error("already_submitted", message, null, submissionId));
    }
}
