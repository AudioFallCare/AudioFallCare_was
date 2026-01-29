package com.bumil.audio_fall_care.domain.internal.dto;

import java.time.LocalDateTime;

public record FallDetectionResponse(
        Long historyId,
        Long alertId,
        Long recorderId,
        Long userId,
        Double confidence,
        String soundType,
        LocalDateTime detectedAt
) {
}
