package com.bumil.audio_fall_care.domain.history.dto;

public record FallHistoryStatsResponse(
        long totalCount,
        long recentWeekCount,
        long recentMonthCount,
        double averageConfidence
) {
}
