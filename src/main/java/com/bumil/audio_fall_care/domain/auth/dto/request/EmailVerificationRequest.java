package com.bumil.audio_fall_care.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record EmailVerificationRequest(
        @NotBlank(message = "이메일을 입력하세요.")
        @Email(message = "이메일 형식이 아닙니다.")
        String email,

        @NotBlank(message = "인증번호를 입력하세요.")
        @Pattern(regexp = "^[0-9]{6}$", message = "인증번호는 6자리 숫자입니다.")
        String code
) {
}
