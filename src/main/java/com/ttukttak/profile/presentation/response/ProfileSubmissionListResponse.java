package com.ttukttak.profile.presentation.response;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.ttukttak.domain.submission.domain.Track;
import com.ttukttak.submission.presentation.response.SubmissionCard;

import java.util.List;

/** 프로필의 제출작 탭. 최신순 */
public record ProfileSubmissionListResponse(List<Item> items, String nextCursor) {

    /** medal: 결과 발표 뒤 트랙 3위 안에 들었을 때만 */
    public record Item(@JsonUnwrapped SubmissionCard card, WeekRef week, Medal medal) {
    }

    public record WeekRef(String code, String title) {
    }

    public record Medal(Track track, int rank) {
    }
}
