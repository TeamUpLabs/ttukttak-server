package com.ttukttak.domain.submission.domain;

import java.util.UUID;

/** 제출작 하나가 받은 표 */
public record VoteTotal(UUID submissionId, Track track, long votes) {
}
