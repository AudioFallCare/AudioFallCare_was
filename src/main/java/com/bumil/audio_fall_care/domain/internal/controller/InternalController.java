package com.bumil.audio_fall_care.domain.internal.controller;

import com.bumil.audio_fall_care.domain.internal.dto.FallDetectionRequest;
import com.bumil.audio_fall_care.domain.internal.dto.FallDetectionResponse;
import com.bumil.audio_fall_care.domain.internal.service.InternalService;
import com.bumil.audio_fall_care.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal")
@RequiredArgsConstructor
public class InternalController {

    private final InternalService internalService;

    /**
     * AI 서버에서 낙상 감지 결과를 수신한다.
     * 인증 불필요 (AI 서버 → Spring 내부 통신)
     */
    @PostMapping("/fall")
    public ResponseEntity<ApiResponse<FallDetectionResponse>> receiveFallDetection(
            @Valid @RequestBody FallDetectionRequest request) {

        FallDetectionResponse response = internalService.processFallDetection(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
