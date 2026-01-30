package com.bumil.audio_fall_care.domain.auth.dto.response;

public record LoginResponse(
        Long userId,
        String username
) {
}
