package com.ttukttak.domain.user.exception;

import com.ttukttak.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class SelfFollowException extends BusinessException {
    public SelfFollowException() {
        super(HttpStatus.FORBIDDEN, "forbidden", "나 자신은 팔로우할 수 없어요.");
    }
}
