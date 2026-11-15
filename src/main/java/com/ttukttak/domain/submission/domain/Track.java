package com.ttukttak.domain.submission.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Track {
    BEGINNER("입문"),
    REGULAR("일반"),
    EXPERT("고수");

    private final String description;
}
