package com.ttukttak.submission.presentation.response;

import java.util.List;

public record SubmissionListResponse(List<SubmissionCard> items, String nextCursor, long total) {
}
