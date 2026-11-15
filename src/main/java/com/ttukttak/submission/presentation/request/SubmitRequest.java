package com.ttukttak.submission.presentation.request;

import com.ttukttak.domain.submission.domain.SubmissionContent;
import com.ttukttak.domain.submission.domain.Track;
import com.ttukttak.submission.presentation.common.SubmissionFields;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

import static com.ttukttak.submission.presentation.common.SubmissionFields.*;

public record SubmitRequest(
        @NotNull(message = "트랙을 골라 주세요.") Track track,

        @NotBlank(message = "제목을 입력해 주세요.") @Size(max = 30, message = "제목은 30자까지 써요.")
        String title,

        @NotNull(message = "한 줄 소개를 입력해 주세요.") @Size(min = 10, max = 80, message = "한 줄 소개는 10–80자로 써요.")
        String summary,

        @NotNull(message = "데모 주소를 입력해 주세요.") @Size(max = 300, message = "데모 주소는 300자까지 써요.")
        @Pattern(regexp = DEMO_URL, flags = Pattern.Flag.CASE_INSENSITIVE, message = DEMO_URL_MESSAGE)
        String demoUrl,

        @NotNull(message = "레포 주소를 입력해 주세요.") @Pattern(regexp = REPO_URL, message = REPO_URL_MESSAGE)
        String repoUrl,

        @NotNull(message = "썸네일을 골라 주세요.") @Valid SubmissionFields.Thumbnail thumbnail,

        @Size(max = 6, message = "스택은 6개까지 써요.")
        List<@NotBlank(message = "스택을 입력해 주세요.") @Size(max = 20, message = "스택은 20자까지 써요.") String> stack,

        @Valid StoryInput story,

        @NotNull(message = "이번 주 규칙을 지켰는지 확인해 주세요.") @AssertTrue(message = "이번 주 규칙을 지켰는지 확인해 주세요.")
        Boolean confirmsRule,

        Boolean demoWithoutLogin
) {
    public SubmitRequest {
        title = strip(title);
        summary = strip(summary);
        demoUrl = strip(demoUrl);
        repoUrl = strip(repoUrl);
    }

    public SubmissionContent toContent() {
        return new SubmissionContent(track, title, summary, demoUrl, repoUrl, thumbnail.toDomain(),
                stack == null ? List.of() : SubmissionFields.stack(stack),
                story == null ? null : story.toDomain(),
                Boolean.TRUE.equals(demoWithoutLogin));
    }
}
