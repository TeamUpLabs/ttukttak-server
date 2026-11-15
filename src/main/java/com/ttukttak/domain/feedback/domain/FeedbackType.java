package com.ttukttak.domain.feedback.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FeedbackType {
    BUG("버그"),
    UX("사용성"),
    CODE("코드"),
    KUDO("칭찬");

    private final String description;
}
