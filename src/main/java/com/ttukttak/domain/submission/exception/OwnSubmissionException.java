package com.ttukttak.domain.submission.exception;

import com.ttukttak.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class OwnSubmissionException extends BusinessException {
    private OwnSubmissionException(String message) {
        super(HttpStatus.FORBIDDEN, "own_submission", message);
    }

    public static OwnSubmissionException vote() {
        return new OwnSubmissionException("내 제출작에는 투표할 수 없어요.");
    }

    public static OwnSubmissionException feedback() {
        return new OwnSubmissionException("내 제출작에는 피드백을 남길 수 없어요.");
    }
}
