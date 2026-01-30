package com.bumil.audio_fall_care.domain.auth.controller;

import com.bumil.audio_fall_care.domain.auth.dto.request.EmailRequest;
import com.bumil.audio_fall_care.domain.auth.dto.request.EmailVerificationRequest;
import com.bumil.audio_fall_care.domain.auth.dto.request.SignUpRequest;
import com.bumil.audio_fall_care.domain.auth.service.AuthService;
import com.bumil.audio_fall_care.domain.user.service.UserService;
import com.bumil.audio_fall_care.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "인증", description = "회원가입, 이메일 인증 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @Operation(summary = "이메일 인증코드 전송", description = "입력한 이메일로 인증코드를 전송합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "이메일 전송 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping("/email/code")
    public ResponseEntity<ApiResponse<String>> sendVerificationCode(
            @Valid @RequestBody EmailRequest dto
            ) {

        authService.sendVerificationCode(dto.email());

        return ResponseEntity.ok(
                ApiResponse.ok("이메일 전송에 성공했습니다.")
        );
    }


    @Operation(summary = "이메일 인증코드 확인", description = "이메일로 전송된 인증코드를 검증합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "인증 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 인증코드")
    })
    @PostMapping("/email/verify")
    public ResponseEntity<ApiResponse<String>> verifyCode(
            @Valid @RequestBody EmailVerificationRequest dto
    ) {
        authService.verifyCode(dto.email(),  dto.code());

        return ResponseEntity.ok(
                ApiResponse.ok("이메일 인증이 완료되었습니다.")
        );
    }


    @Operation(summary = "회원가입", description = "새로운 보호자 계정을 생성합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 존재하는 이메일")
    })
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Long>> signUp(
            @Valid @RequestBody SignUpRequest dto
            ) {

        Long userId = userService.signUp(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(userId));
    }
}
