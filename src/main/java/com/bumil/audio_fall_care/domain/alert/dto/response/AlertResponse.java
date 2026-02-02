package com.bumil.audio_fall_care.domain.alert.dto.response;

import com.bumil.audio_fall_care.domain.alert.entity.Alert;
import com.bumil.audio_fall_care.domain.alert.entity.AlertType;

import java.time.LocalDateTime;

public record AlertResponse(
        Long id,
        AlertType type,
        String message,
        boolean isRead,
        LocalDateTime createdAt
) {
    public static AlertResponse from(Alert alert) {
        return new AlertResponse(
                alert.getId(),
                alert.getType(),
                alert.getMessage(),
                alert.getIsRead(),
                alert.getCreatedAt()
        );
    }
}
