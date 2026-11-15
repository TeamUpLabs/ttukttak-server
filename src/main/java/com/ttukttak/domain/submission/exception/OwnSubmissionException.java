package com.ttukttak.domain.submission.exception;

import com.ttukttak.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class OwnSubmissionException extends BusinessException {
    public OwnSubmissionException() {
        super(HttpStatus.FORBIDDEN, "own_submission", "내 제출작에는 투표할 수 없어요.");
    }
}
