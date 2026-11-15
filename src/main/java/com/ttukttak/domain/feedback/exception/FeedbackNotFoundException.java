package com.ttukttak.domain.feedback.exception;

import com.ttukttak.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class FeedbackNotFoundException extends BusinessException {
    public FeedbackNotFoundException() {
        super(HttpStatus.NOT_FOUND, "not_found", "피드백을 찾을 수 없어요.");
    }
}
