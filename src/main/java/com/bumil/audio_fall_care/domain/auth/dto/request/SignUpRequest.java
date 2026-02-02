package com.bumil.audio_fall_care.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignUpRequest(
        @NotBlank(message = "아이디를 입력하세요.")
        @Size(min = 4, max = 20, message = "아이디는 4~20자여야 합니다.")
        @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "아이디는 영문과 숫자만 가능합니다.")
        String username,

        @NotBlank(message = "비밀번호를 입력하세요.")
        @Size(min = 8, message = "비밀번호 8자리 이상 입력하세요.")
        String password,

        @NotBlank(message = "비밀번호 확인을 입력하세요.")
        String passwordConfirm,

        @NotBlank(message = "우편번호를 입력하세요")
        @Pattern(regexp = "^[0-9]{5}$", message = "우편번호는 숫자 5자리여야 합니다.")
        String zipcode,

        @NotBlank(message = "주소를 입력하세요.")
        String address,

        String addressDetail
) { }
