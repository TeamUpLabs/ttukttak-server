package com.ttukttak.domain.week.exception;

import com.ttukttak.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class ScheduleLockedException extends BusinessException {
    public ScheduleLockedException() {
        super(HttpStatus.CONFLICT, "conflict", "공개된 주차의 일정을 바꾸려면 force: true를 보내 주세요.");
    }
}
