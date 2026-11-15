package com.ttukttak.domain.submission.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SubmissionSort {
    VOTES("투표순"),
    RECENT("최신순"),
    RANDOM("무작위");

    private final String description;
}
