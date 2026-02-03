package com.bumil.audio_fall_care.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(
        @NotBlank(message = "디바이스 정보를 입력하세요.")
        String deviceInfo
) {
}
