package com.ttukttak.domain.feedback.exception;

import com.ttukttak.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class FeedbackForbiddenException extends BusinessException {
    private FeedbackForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, "forbidden", message);
    }

    public static FeedbackForbiddenException notAuthor() {
        return new FeedbackForbiddenException("내 피드백만 고칠 수 있어요.");
    }

    public static FeedbackForbiddenException editClosed() {
        return new FeedbackForbiddenException("작성 후 24시간이 지나 고칠 수 없어요.");
    }

    public static FeedbackForbiddenException ownFeedback() {
        return new FeedbackForbiddenException("내 피드백에는 도움돼요를 누를 수 없어요.");
    }

    public static FeedbackForbiddenException notSubmissionAuthor() {
        return new FeedbackForbiddenException("제출작을 만든 사람만 답글을 달 수 있어요.");
    }
}
