package com.ttukttak.feedback.presentation.response;

/** pointsEarned: 이번 피드백으로 쌓인 포인트, points: 쌓인 합계 */
public record CreateFeedbackResponse(FeedbackItem feedback, long pointsEarned, long points) {
}
