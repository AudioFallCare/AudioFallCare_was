package com.bumil.audio_fall_care.domain.recorder.controller;

import com.bumil.audio_fall_care.domain.recorder.dto.RecorderRegisterRequest;
import com.bumil.audio_fall_care.domain.recorder.dto.RecorderResponse;
import com.bumil.audio_fall_care.domain.recorder.dto.RecorderUpdateRequest;
import com.bumil.audio_fall_care.domain.recorder.dto.RecorderUserResponse; // Import new DTO
import com.bumil.audio_fall_care.domain.recorder.service.RecorderService;
import com.bumil.audio_fall_care.global.common.ApiResponse;
import com.bumil.audio_fall_care.global.security.CustomUserDetails; // Keep CustomUserDetails for other methods
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

    @Operation(summary = "리코더 등록", description = "연결 코드를 검증하고 리코더를 등록합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "등록 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효하지 않은 연결 코드"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "연결 코드를 찾을 수 없음")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<RecorderResponse>> registerRecorder(
            @Valid @RequestBody RecorderRegisterRequest request) {

        RecorderResponse recorder = recorderService.registerRecorder(request);
        return ResponseEntity.ok(ApiResponse.ok(recorder));
    }

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

    @Operation(summary = "리코더 정보 수정", description = "리코더의 디바이스 이름을 설정합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "리코더를 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<RecorderResponse>> updateRecorder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody RecorderUpdateRequest request) {

        RecorderResponse recorder = recorderService.updateRecorder(userDetails.getUserId(), id, request);
        return ResponseEntity.ok(ApiResponse.ok(recorder));
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

    @Operation(summary = "리코더와 연결된 사용자 정보 조회", description = "특정 리코더에 연결된 사용자의 정보를 조회합니다. 이 사용자는 리코더의 보호자로 간주됩니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "리코더를 찾을 수 없음")
    })
    @GetMapping("/{recorderId}/user")
    public ResponseEntity<ApiResponse<RecorderUserResponse>> getRecorderUser(
            @PathVariable Long recorderId) {

        RecorderUserResponse recorderUserResponse = recorderService.getRecorderUser(recorderId);
        return ResponseEntity.ok(ApiResponse.ok(recorderUserResponse));
    }
}
