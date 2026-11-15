package com.ttukttak.domain.submission.exception;

import com.ttukttak.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class NotAuthorException extends BusinessException {
    public NotAuthorException() {
        super(HttpStatus.FORBIDDEN, "forbidden", "내 제출작만 고칠 수 있어요.");
    }
}
