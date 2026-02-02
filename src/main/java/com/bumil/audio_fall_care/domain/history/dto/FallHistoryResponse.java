package com.bumil.audio_fall_care.domain.history.dto;

import com.bumil.audio_fall_care.domain.history.entity.FallHistory;

import java.time.LocalDateTime;

public record FallHistoryResponse(
        Long id,
        Long recorderId,
        String deviceName,
        Double confidence,
        String soundType,
        LocalDateTime detectedAt
) {
    public static FallHistoryResponse from(FallHistory history) {
        return new FallHistoryResponse(
                history.getId(),
                history.getRecorder().getId(),
                history.getRecorder().getDeviceName(),
                history.getConfidence(),
                history.getSoundType(),
                history.getDetectedAt()
        );
    }
}
