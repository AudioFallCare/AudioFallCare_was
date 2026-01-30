package com.bumil.audio_fall_care.domain.code.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CodeVerifyRequest(
        @NotBlank(message = "연결 코드를 입력하세요.")
        String code
) {
}
