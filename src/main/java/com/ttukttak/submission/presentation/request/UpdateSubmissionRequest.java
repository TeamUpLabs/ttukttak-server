package com.ttukttak.submission.presentation.request;

import com.ttukttak.domain.submission.domain.SubmissionContent;
import com.ttukttak.domain.submission.domain.Track;
import com.ttukttak.submission.presentation.common.SubmissionFields;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

import static com.ttukttak.submission.presentation.common.SubmissionFields.*;

/** 보낸 필드만 바뀌어요. 규칙은 SubmitRequest와 같아요. */
// ponytail: pinnedCommits는 커밋 API를 만들 때 받아요.
public record UpdateSubmissionRequest(
        Track track,

        // null은 "안 바꿈"이라 @NotBlank 대신 @Pattern으로 공백만 있는 제목을 막아요.
        @Pattern(regexp = "(?s).*\\S.*", message = "제목을 입력해 주세요.") @Size(max = 30, message = "제목은 30자까지 써요.")
        String title,

        @Size(min = 10, max = 80, message = "한 줄 소개는 10–80자로 써요.")
        String summary,

        @Size(max = 300, message = "데모 주소는 300자까지 써요.")
        @Pattern(regexp = DEMO_URL, flags = Pattern.Flag.CASE_INSENSITIVE, message = DEMO_URL_MESSAGE)
        String demoUrl,

        @Pattern(regexp = REPO_URL, message = REPO_URL_MESSAGE)
        String repoUrl,

        @Valid SubmissionFields.Thumbnail thumbnail,

        @Size(max = 6, message = "스택은 6개까지 써요.")
        List<@NotBlank(message = "스택을 입력해 주세요.") @Size(max = 20, message = "스택은 20자까지 써요.") String> stack,

        @Valid StoryInput story,

        @AssertTrue(message = "이번 주 규칙을 지켰는지 확인해 주세요.")
        Boolean confirmsRule,

        Boolean demoWithoutLogin
) {
    public UpdateSubmissionRequest {
        title = strip(title);
        summary = strip(summary);
        demoUrl = strip(demoUrl);
        repoUrl = strip(repoUrl);
    }

    public SubmissionContent toPatch() {
        return new SubmissionContent(track, title, summary, demoUrl, repoUrl,
                thumbnail == null ? null : thumbnail.toDomain(),
                SubmissionFields.stack(stack),
                story == null ? null : story.toDomain(),
                demoWithoutLogin);
    }
}
