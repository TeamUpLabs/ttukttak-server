package com.ttukttak.domain.submission.domain;

import java.util.*;

/** 한 주의 투표 결과. 표가 많은 순, 동점이면 먼저 낸 순으로 순위를 매겨요. */
public final class Scoreboard {
    public record Score(long votes, int trackRank, int overallRank) {
    }

    private static final Score NONE = new Score(0, 0, 0);

    private final Map<UUID, Score> scores = new HashMap<>();
    private final Map<Track, List<UUID>> byTrack = new EnumMap<>(Track.class);

    /** totals: 이미 표 많은 순, 먼저 낸 순으로 정렬된 것 */
    public Scoreboard(List<VoteTotal> totals) {
        for (VoteTotal total : totals) {
            List<UUID> track = byTrack.computeIfAbsent(total.track(), t -> new ArrayList<>());
            track.add(total.submissionId());
            scores.put(total.submissionId(), new Score(total.votes(), track.size(), scores.size() + 1));
        }
    }

    public Score get(UUID submissionId) {
        return scores.getOrDefault(submissionId, NONE);
    }

    public long totalVotes() {
        return scores.values().stream().mapToLong(Score::votes).sum();
    }

    /** 트랙별 상위 3개 */
    public List<UUID> podium(Track track) {
        List<UUID> ids = byTrack.getOrDefault(track, List.of());
        return ids.subList(0, Math.min(3, ids.size()));
    }
}
