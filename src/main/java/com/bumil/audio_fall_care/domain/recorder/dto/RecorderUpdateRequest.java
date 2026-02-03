package com.bumil.audio_fall_care.domain.recorder.dto;

import jakarta.validation.constraints.NotBlank;

public record RecorderUpdateRequest(
        @NotBlank(message = "디바이스 이름을 입력하세요.")
        String deviceName
) {
}
