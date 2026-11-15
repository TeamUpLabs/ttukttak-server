package com.ttukttak.domain.feedback.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FeedbackSort {
    HELPFUL("도움순"),
    RECENT("최신순");

    private final String description;
}
