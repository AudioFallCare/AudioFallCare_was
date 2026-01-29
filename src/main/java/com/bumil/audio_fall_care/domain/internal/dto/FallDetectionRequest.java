package com.bumil.audio_fall_care.domain.internal.dto;

import jakarta.validation.constraints.NotNull;

public record FallDetectionRequest(

        @NotNull(message = "recorderId는 필수입니다")
        Long recorderId,

        @NotNull(message = "confidence는 필수입니다")
        Double confidence,

        @NotNull(message = "soundType은 필수입니다")
        String soundType
) {
}
