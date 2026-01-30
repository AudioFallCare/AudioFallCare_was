package com.bumil.audio_fall_care.domain.fcm.controller;

import com.bumil.audio_fall_care.domain.fcm.dto.FcmTokenRequest;
import com.bumil.audio_fall_care.domain.fcm.service.FcmTokenServiceInterface;
import com.bumil.audio_fall_care.global.common.ApiResponse;
import com.bumil.audio_fall_care.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fcm")
@Slf4j
@RequiredArgsConstructor
public class FcmController {

    private final FcmTokenServiceInterface fcmTokenService;

    // 클라이언트 토큰 등록
    @PostMapping("/token")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> registerToken(@RequestBody FcmTokenRequest request,
                                                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            fcmTokenService.saveToken(
                    userDetails.getUserId(),
                    request
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("TOKEN_SAVE_FAILED", e.getMessage())
            );
        }
    }
}