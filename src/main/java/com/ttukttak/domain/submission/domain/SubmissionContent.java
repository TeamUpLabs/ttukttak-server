package com.ttukttak.domain.submission.domain;

import java.util.List;

/** 작성자가 쓰는 칸들. 수정할 땐 null인 칸을 "안 바꿈"으로 봐요. */
public record SubmissionContent(Track track, String title, String summary, String demoUrl, String repoUrl,
                                Keycap keycap, List<String> stack, Story story, Boolean demoWithoutLogin) {

    SubmissionContent merge(SubmissionContent patch) {
        return new SubmissionContent(
                or(patch.track, track), or(patch.title, title), or(patch.summary, summary),
                or(patch.demoUrl, demoUrl), or(patch.repoUrl, repoUrl), or(patch.keycap, keycap),
                or(patch.stack, stack), or(patch.story, story), or(patch.demoWithoutLogin, demoWithoutLogin));
    }

    private static <T> T or(T value, T fallback) {
        return value != null ? value : fallback;
    }
}
