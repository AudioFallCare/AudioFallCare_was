package com.bumil.audio_fall_care.domain.history.service;

import com.bumil.audio_fall_care.domain.history.dto.FallHistoryResponse;
import com.bumil.audio_fall_care.domain.history.dto.FallHistoryStatsResponse;
import com.bumil.audio_fall_care.domain.history.entity.FallHistory;
import com.bumil.audio_fall_care.domain.history.repository.FallHistoryRepository;
import com.bumil.audio_fall_care.global.common.BusinessException;
import com.bumil.audio_fall_care.global.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FallHistoryService {

    private final FallHistoryRepository fallHistoryRepository;

    public List<FallHistoryResponse> getHistories(Long userId) {
        return fallHistoryRepository.findByUserIdOrderByDetectedAtDesc(userId).stream()
                .map(FallHistoryResponse::from)
                .toList();
    }

    public FallHistoryResponse getHistory(Long userId, Long historyId) {
        FallHistory history = fallHistoryRepository.findById(historyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FALL_HISTORY_NOT_FOUND));

        if (!history.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FALL_HISTORY_NOT_OWNED);
        }

        return FallHistoryResponse.from(history);
    }

    @Transactional
    public void deleteHistory(Long userId, Long historyId) {
        FallHistory history = fallHistoryRepository.findById(historyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FALL_HISTORY_NOT_FOUND));

        if (!history.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FALL_HISTORY_NOT_OWNED);
        }

        fallHistoryRepository.delete(history);
    }

    public FallHistoryStatsResponse getStats(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        long totalCount = fallHistoryRepository.countByUserId(userId);
        long recentWeekCount = fallHistoryRepository.countByUserIdAndDetectedAtAfter(userId, now.minusDays(7));
        long recentMonthCount = fallHistoryRepository.countByUserIdAndDetectedAtAfter(userId, now.minusDays(30));
        double averageConfidence = fallHistoryRepository.findAverageConfidenceByUserId(userId);

        return new FallHistoryStatsResponse(totalCount, recentWeekCount, recentMonthCount, averageConfidence);
    }
}
