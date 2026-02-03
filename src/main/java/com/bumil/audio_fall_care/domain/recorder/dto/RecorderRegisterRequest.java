package com.bumil.audio_fall_care.domain.recorder.dto;

import jakarta.validation.constraints.NotBlank;

public record RecorderRegisterRequest(
        @NotBlank(message = "연결 코드를 입력하세요.")
        String code
) {
}
