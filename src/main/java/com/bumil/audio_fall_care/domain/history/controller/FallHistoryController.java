package com.bumil.audio_fall_care.domain.history.controller;

import com.bumil.audio_fall_care.domain.history.dto.FallHistoryResponse;
import com.bumil.audio_fall_care.domain.history.dto.FallHistoryStatsResponse;
import com.bumil.audio_fall_care.domain.history.service.FallHistoryService;
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

@Tag(name = "낙상 이력", description = "낙상 감지 이력 관리 API")
@RestController
@RequestMapping("/api/histories")
@RequiredArgsConstructor
public class FallHistoryController {

    private final FallHistoryService fallHistoryService;

    @Operation(summary = "낙상 이력 목록 조회", description = "로그인한 사용자의 낙상 감지 이력을 최신순으로 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<FallHistoryResponse>>> getHistories(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<FallHistoryResponse> histories = fallHistoryService.getHistories(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(histories));
    }

    @Operation(summary = "낙상 통계 조회", description = "로그인한 사용자의 낙상 감지 통계를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<FallHistoryStatsResponse>> getStats(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        FallHistoryStatsResponse stats = fallHistoryService.getStats(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(stats));
    }

    @Operation(summary = "낙상 이력 상세 조회", description = "특정 낙상 감지 이력의 상세 정보를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "낙상 이력을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FallHistoryResponse>> getHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {

        FallHistoryResponse history = fallHistoryService.getHistory(userDetails.getUserId(), id);
        return ResponseEntity.ok(ApiResponse.ok(history));
    }

    @Operation(summary = "낙상 이력 삭제", description = "특정 낙상 감지 이력을 삭제합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "낙상 이력을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {

        fallHistoryService.deleteHistory(userDetails.getUserId(), id);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
