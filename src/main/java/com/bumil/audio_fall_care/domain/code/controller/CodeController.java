package com.bumil.audio_fall_care.domain.code.controller;

import com.bumil.audio_fall_care.domain.code.dto.request.CodeVerifyRequest;
import com.bumil.audio_fall_care.domain.code.dto.response.CodeGenerateResponse;
import com.bumil.audio_fall_care.domain.code.dto.response.CodeVerifyResponse;
import com.bumil.audio_fall_care.domain.code.service.CodeService;
import com.bumil.audio_fall_care.global.common.ApiResponse;
import com.bumil.audio_fall_care.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "연결 코드", description = "연결 코드 발급/검증 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/code")
public class CodeController {

    private final CodeService codeService;

    @Operation(summary = "연결 코드 발급", description = "기존 코드가 있으면 기존 코드를 반환하고, 없으면 새로 발급합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "코드 반환/발급 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @PostMapping("/generate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CodeGenerateResponse>> generateCode(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        CodeGenerateResponse response = codeService.generateCode(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "연결 코드 재발급", description = "기존 코드를 삭제하고 새 코드를 발급합니다. 기존 리코더 연결이 해제됩니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "코드 재발급 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @PostMapping("/regenerate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CodeGenerateResponse>> regenerateCode(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        CodeGenerateResponse response = codeService.regenerateCode(userDetails.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response));
    }

    @Operation(summary = "연결 코드 검증", description = "리코더에서 입력한 연결 코드를 검증합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "코드 검증 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "만료 또는 사용된 코드"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "코드를 찾을 수 없음")
    })
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<CodeVerifyResponse>> verifyCode(
            @Valid @RequestBody CodeVerifyRequest request
    ) {
        CodeVerifyResponse response = codeService.verifyCode(request.code());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

}
