package com.bumil.audio_fall_care.domain.auth.dto.response;

public record TokenPair(
        String refreshToken,
        String accessToken
) {
}
