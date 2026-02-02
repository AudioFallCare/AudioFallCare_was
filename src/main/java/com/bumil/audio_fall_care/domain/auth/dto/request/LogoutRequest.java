package com.bumil.audio_fall_care.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(
        @NotBlank
        String deviceInfo
) {
}
