package com.ttukttak.common;

import com.ttukttak.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

/**
 * 목록 커서. "몇 번째부터:무작위 씨앗"을 base64로 담아요.
 * 무작위 순서가 페이지를 넘겨도 섞이지 않게 씨앗을 이어받아요. 씨앗을 안 쓰는 목록은 무시하면 돼요.
 */
// ponytail: offset이라 순서가 바뀌는 중엔 겹치거나 빠질 수 있어요. 문제되면 키셋 커서로.
public record OffsetCursor(int offset, String seed) {

    /** null이면 처음부터, 새 씨앗으로 */
    public static OffsetCursor decode(String cursor) {
        if (cursor == null) return new OffsetCursor(0, UUID.randomUUID().toString().substring(0, 8));
        try {
            String[] parts = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8).split(":", 2);
            int offset = Integer.parseInt(parts[0]);
            if (offset < 0 || parts.length < 2) throw new IllegalArgumentException();
            return new OffsetCursor(offset, parts[1]);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "invalid_request", "목록 위치가 올바르지 않아요.");
        }
    }

    public OffsetCursor next(int size) {
        return new OffsetCursor(offset + size, seed);
    }

    public String encode() {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString((offset + ":" + seed).getBytes(StandardCharsets.UTF_8));
    }
}
