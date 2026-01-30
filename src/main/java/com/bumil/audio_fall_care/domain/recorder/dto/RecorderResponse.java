package com.bumil.audio_fall_care.domain.recorder.dto;

import com.bumil.audio_fall_care.domain.recorder.entity.Recorder;
import com.bumil.audio_fall_care.domain.recorder.entity.RecorderStatus;

import java.time.LocalDateTime;

public record RecorderResponse(
        Long id,
        String deviceName,
        RecorderStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static RecorderResponse from(Recorder recorder) {
        return new RecorderResponse(
                recorder.getId(),
                recorder.getDeviceName(),
                recorder.getStatus(),
                recorder.getCreatedAt(),
                recorder.getUpdatedAt()
        );
    }
}
