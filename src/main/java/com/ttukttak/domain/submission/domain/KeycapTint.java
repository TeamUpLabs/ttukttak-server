package com.ttukttak.domain.submission.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum KeycapTint {
    SAGE("세이지"),
    SKY("하늘"),
    SAND("모래"),
    BUTTER("버터"),
    NIGHT("밤"),
    SIGNAL("신호");

    private final String description;
}
