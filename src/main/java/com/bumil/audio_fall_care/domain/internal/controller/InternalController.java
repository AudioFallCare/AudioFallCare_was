package com.bumil.audio_fall_care.domain.internal.controller;

import com.bumil.audio_fall_care.domain.internal.dto.FallDetectionRequest;
import com.bumil.audio_fall_care.domain.internal.dto.FallDetectionResponse;
import com.bumil.audio_fall_care.domain.internal.service.InternalService;
import com.bumil.audio_fall_care.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "내부 통신", description = "AI 서버 → Spring 내부 통신 API")
@RestController
@RequestMapping("/api/internal")
@RequiredArgsConstructor
public class InternalController {

    private final InternalService internalService;

    @Operation(summary = "낙상 감지 결과 수신", description = "AI 서버에서 낙상 감지 결과를 수신합니다. 인증 불필요 (AI 서버 → Spring 내부 통신)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "처리 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping("/fall")
    public ResponseEntity<ApiResponse<FallDetectionResponse>> receiveFallDetection(
            @Valid @RequestBody FallDetectionRequest request) {

        FallDetectionResponse response = internalService.processFallDetection(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
