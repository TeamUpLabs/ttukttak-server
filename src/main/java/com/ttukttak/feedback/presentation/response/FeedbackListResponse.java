package com.ttukttak.feedback.presentation.response;

import com.ttukttak.domain.feedback.domain.FeedbackType;

import java.util.List;
import java.util.Map;

/** counts로 필터 탭의 숫자를 그려요. */
public record FeedbackListResponse(List<FeedbackItem> items, Counts counts, String nextCursor) {

    public record Counts(long all, long bug, long ux, long code, long kudo) {
        public static Counts from(Map<FeedbackType, Long> byType) {
            long bug = byType.getOrDefault(FeedbackType.BUG, 0L), ux = byType.getOrDefault(FeedbackType.UX, 0L),
                    code = byType.getOrDefault(FeedbackType.CODE, 0L), kudo = byType.getOrDefault(FeedbackType.KUDO, 0L);
            return new Counts(bug + ux + code + kudo, bug, ux, code, kudo);
        }
    }
}
