package com.bumil.audio_fall_care.domain.auth.dto.response;

public record SignUpResponse(
        Long userId,
        String code
) {
}
