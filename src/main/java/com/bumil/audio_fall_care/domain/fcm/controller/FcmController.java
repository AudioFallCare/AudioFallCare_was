package com.bumil.audio_fall_care.domain.fcm.controller;

import com.bumil.audio_fall_care.domain.fcm.dto.FcmTokenRequest;
import com.bumil.audio_fall_care.domain.fcm.service.FcmTokenServiceInterface;
import com.bumil.audio_fall_care.global.common.ApiResponse;
import com.bumil.audio_fall_care.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(
            summary = "FCM 토큰 등록",
            description = """
                    푸시 알림을 받기 위한 FCM 토큰을 서버에 등록합니다.
                    앱 설치/재설치 시 또는 토큰 갱신 시 호출해야 합니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "토큰 등록 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/token")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> registerToken(@RequestBody FcmTokenRequest request,
                                                           @AuthenticationPrincipal CustomUserDetails userDetails) {
            fcmTokenService.saveToken(
                    userDetails.getUserId(),
                    request
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok());
    }
}