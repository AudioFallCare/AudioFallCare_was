package com.bumil.audio_fall_care.domain.code.dto.response;

import java.time.LocalDateTime;

public record CodeGenerateResponse(
        Long id,
        String code,
        LocalDateTime expiresAt
) {
}
