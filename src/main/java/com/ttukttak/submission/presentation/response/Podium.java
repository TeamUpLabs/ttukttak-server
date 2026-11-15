package com.ttukttak.submission.presentation.response;

import java.util.List;

/** 주차 결과의 트랙별 상위 3개. 표 많은 순, 동점이면 먼저 낸 순이에요. */
public record Podium(List<SubmissionCard> beginner, List<SubmissionCard> regular, List<SubmissionCard> expert) {
}
