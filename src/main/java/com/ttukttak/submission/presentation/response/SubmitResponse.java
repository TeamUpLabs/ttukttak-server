package com.ttukttak.submission.presentation.response;

/** POST /weeks/:weekId/submissions 응답. 제출 완료 화면의 "N주 연속 제출"을 streak으로 그려요. */
public record SubmitResponse(SubmissionDetailResponse.Detail submission, Streak streak) {

    public record Streak(int current, int best, boolean isNewBest) {
    }
}
