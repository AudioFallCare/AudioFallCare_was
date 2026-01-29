package com.bumil.audio_fall_care.domain.auth.controller;

import com.bumil.audio_fall_care.domain.auth.dto.request.EmailRequest;
import com.bumil.audio_fall_care.domain.auth.dto.request.EmailVerificationRequest;
import com.bumil.audio_fall_care.domain.auth.dto.request.SignUpRequest;
import com.bumil.audio_fall_care.domain.auth.service.AuthService;
import com.bumil.audio_fall_care.domain.user.service.UserService;
import com.bumil.audio_fall_care.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private AuthService authService;
    private UserService userService;

    @PostMapping("/email/code")
    public ResponseEntity<ApiResponse<String>> sendVerificationCode(
            @Valid @RequestBody EmailRequest dto
            ) {

        authService.sendVerificationCode(dto.email());

        return ResponseEntity.ok(
                ApiResponse.ok("이메일 전송에 성공했습니다.")
        );
    }


    @PostMapping("/email/verify")
    public ResponseEntity<ApiResponse<String>> verifyCode(
            @Valid @RequestBody EmailVerificationRequest dto
    ) {
        authService.verifyCode(dto.email(),  dto.code());

        return ResponseEntity.ok(
                ApiResponse.ok("이메일 인증이 완료되었습니다.")
        );
    }


    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Long>> signUp(
            @Valid @RequestBody SignUpRequest dto
            ) {

        Long userId = userService.signUp(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(userId));
    }
}
