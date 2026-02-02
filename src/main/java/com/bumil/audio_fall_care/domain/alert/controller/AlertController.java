package com.bumil.audio_fall_care.domain.alert.controller;

import com.bumil.audio_fall_care.domain.alert.dto.response.AlertResponse;
import com.bumil.audio_fall_care.domain.alert.service.AlertService;
import com.bumil.audio_fall_care.global.common.ApiResponse;
import com.bumil.audio_fall_care.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<AlertResponse>>> findAllByUserId(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<AlertResponse> alerts = alertService.findAllByUserId(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(alerts));
    }
}
