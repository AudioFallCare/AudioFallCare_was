package com.bumil.audio_fall_care.domain.fcm.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record FcmTokenRequest(

        @NotBlank(message = "FCM token은 필수입니다.")
        @Length(max = 500, message = "FCM token은 최대 500자까지 가능합니다.")
        String token,

        @Length(max = 255, message = "디바이스 정보는 최대 255자까지 가능합니다.")
        String deviceInfo
) {
}
