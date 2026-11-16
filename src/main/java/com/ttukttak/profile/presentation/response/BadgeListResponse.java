package com.ttukttak.profile.presentation.response;

import com.ttukttak.domain.profile.domain.Badge;
import com.ttukttak.domain.profile.domain.BadgeKind;
import com.ttukttak.week.presentation.common.WeekSchedule;

import java.time.OffsetDateTime;
import java.util.List;

/** 받은 순서대로. 이름·설명·색은 프론트가 kind와 value로 정해요. */
public record BadgeListResponse(List<Item> items) {

    public record Item(String id, BadgeKind kind, int value, String unit, OffsetDateTime earnedAt) {
        static Item from(Badge badge) {
            return new Item(badge.id(), badge.kind(), badge.value(), badge.kind().getUnit(),
                    WeekSchedule.kst(badge.earnedAt()));
        }
    }

    public static BadgeListResponse from(List<Badge> badges) {
        return new BadgeListResponse(badges.stream().map(Item::from).toList());
    }
}
