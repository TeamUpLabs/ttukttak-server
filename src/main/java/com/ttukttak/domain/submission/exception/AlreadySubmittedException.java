package com.ttukttak.domain.submission.exception;

import com.ttukttak.common.exception.BusinessException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.UUID;

/** 응답에 이미 낸 제출작 id를 함께 실어요. 프론트가 수정 모드로 열 때 써요. */
@Getter
public class AlreadySubmittedException extends BusinessException {
    private final UUID submissionId;

    public AlreadySubmittedException(UUID submissionId) {
        super(HttpStatus.CONFLICT, "already_submitted", "이번 주에 이미 제출했어요.");
        this.submissionId = submissionId;
    }
}
