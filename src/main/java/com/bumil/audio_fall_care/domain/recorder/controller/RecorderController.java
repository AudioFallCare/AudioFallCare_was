package com.bumil.audio_fall_care.domain.recorder.controller;

import com.bumil.audio_fall_care.domain.recorder.dto.RecorderResponse;
import com.bumil.audio_fall_care.domain.recorder.service.RecorderService;
import com.bumil.audio_fall_care.global.common.ApiResponse;
import com.bumil.audio_fall_care.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "리코더", description = "리코더 관리 API")
@RestController
@RequestMapping("/api/recorders")
@RequiredArgsConstructor
public class RecorderController {

    private final RecorderService recorderService;

    @Operation(summary = "연결된 리코더 목록 조회", description = "로그인한 사용자의 연결된 리코더 목록을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<RecorderResponse>>> getRecorders(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<RecorderResponse> recorders = recorderService.getRecorders(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(recorders));
    }

    @Operation(summary = "리코더 연결 해제", description = "리코더 연결을 해제합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "연결 해제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "리코더를 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRecorder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {

        recorderService.deleteRecorder(userDetails.getUserId(), id);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @Operation(summary = "리코더 상태 조회", description = "특정 리코더의 상태를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "리코더를 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @GetMapping("/{id}/status")
    public ResponseEntity<ApiResponse<RecorderResponse>> getRecorderStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {

        RecorderResponse recorder = recorderService.getRecorderStatus(userDetails.getUserId(), id);
        return ResponseEntity.ok(ApiResponse.ok(recorder));
    }
}
