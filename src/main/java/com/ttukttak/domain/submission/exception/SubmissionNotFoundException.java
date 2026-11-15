package com.ttukttak.domain.submission.exception;

import com.ttukttak.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class SubmissionNotFoundException extends BusinessException {
    public SubmissionNotFoundException() {
        super(HttpStatus.NOT_FOUND, "not_found", "제출작을 찾을 수 없어요.");
    }
}
