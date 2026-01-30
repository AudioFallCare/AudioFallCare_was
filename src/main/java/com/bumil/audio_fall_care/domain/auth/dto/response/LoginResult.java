package com.bumil.audio_fall_care.domain.auth.dto.response;

public record LoginResult(
        Long userId,
        String username,
        TokenPair tokenPair
) {
}
