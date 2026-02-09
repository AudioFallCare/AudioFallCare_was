package com.bumil.audio_fall_care.domain.alert.controller;

import com.bumil.audio_fall_care.domain.alert.dto.response.AlertResponse;
import com.bumil.audio_fall_care.domain.alert.entity.FallDiff;
import com.bumil.audio_fall_care.domain.alert.service.AlertService;
import com.bumil.audio_fall_care.global.common.ApiResponse;
import com.bumil.audio_fall_care.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "알림", description = "알림 조회 API")
@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @Operation(
            summary = "알림 목록 조회",
            description = "로그인한 사용자에게 온 알림 목록을 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<AlertResponse>>> findAllByUserId(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<AlertResponse> alerts = alertService.findAllByUserId(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(alerts));
    }

    @Operation(
            summary = "읽지 않은 알림 개수 조회",
            description = "로그인한 사용자에게 온 알림 중 읽지 않은 알림의 개수를 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "처리 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/unread/count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Long>> countUnreadAlerts(@AuthenticationPrincipal CustomUserDetails userDetails) {
        long count = alertService.countUnreadAlerts(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(count));
    }

    @Operation(
            summary = "지난달 대비 낙상 발생 빈도 비교 조회",
            description = "로그인한 사용자와 연결된 리코더의 지난달과 이번달 낙상 발생 건수를 비교하여 증감 정보를 반환합니다"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "처리 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/fall-diff")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<FallDiff>> compareLastMonth(@AuthenticationPrincipal CustomUserDetails userDetails) {
        FallDiff fallDiff = alertService.compareToLastMonth(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(fallDiff));
    }

    @Operation(
            summary = "알림 읽음 처리",
            description = "로그인한 사용자에게 온 알림을 읽음 처리합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "처리 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PatchMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markAsRead(@PathVariable("id") Long alertId,
                                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        alertService.markAsRead(alertId, userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }
}
