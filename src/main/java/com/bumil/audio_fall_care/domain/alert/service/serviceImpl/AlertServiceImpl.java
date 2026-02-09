package com.bumil.audio_fall_care.domain.alert.service.serviceImpl;

import com.bumil.audio_fall_care.domain.alert.dto.response.AlertResponse;
import com.bumil.audio_fall_care.domain.alert.entity.Alert;
import com.bumil.audio_fall_care.domain.alert.entity.FallDiff;
import com.bumil.audio_fall_care.domain.alert.repository.AlertRepository;
import com.bumil.audio_fall_care.domain.alert.service.AlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AlertServiceImpl implements AlertService {

    private final AlertRepository alertRepository;

    @Override
    public List<AlertResponse> findAllByUserId(Long userId) {
        return alertRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(AlertResponse::from)
                .toList();
    }

    @Override
    public long countUnreadAlerts(Long userId) {
        return alertRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    @Override
    public void markAsRead(Long alertId, Long userId) {
        alertRepository.findByIdAndUserId(alertId, userId)
                .ifPresent(Alert::markAsRead);
    }

    @Override
    public FallDiff compareToLastMonth(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        YearMonth thisMonth = YearMonth.from(now);
        YearMonth lastMonth = thisMonth.minusMonths(1);

        // 지난달 전체 기간
        LocalDateTime lastMonthStart = lastMonth.atDay(1).atStartOfDay();
        LocalDateTime lastMonthEnd = lastMonth.atEndOfMonth().atTime(23, 59, 59);

        // 이번달 1일 ~ 현재까지
        LocalDateTime thisMonthStart = thisMonth.atDay(1).atStartOfDay();

        long countLastMonth = alertRepository.countByUserIdAndCreatedAtBetween(
                userId,
                lastMonthStart,
                lastMonthEnd
        );

        long countThisMonth = alertRepository.countByUserIdAndCreatedAtBetween(
                userId,
                thisMonthStart,
                now
        );

        long diff = countLastMonth - countThisMonth;

        if (diff < 0) {
            return FallDiff.INCREASE;
        } else if (diff > 0) {
            return FallDiff.DECREASE;
        }

        return FallDiff.SAME;
    }
}
