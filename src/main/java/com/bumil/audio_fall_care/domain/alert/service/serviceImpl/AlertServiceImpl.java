package com.bumil.audio_fall_care.domain.alert.service.serviceImpl;

import com.bumil.audio_fall_care.domain.alert.dto.response.AlertResponse;
import com.bumil.audio_fall_care.domain.alert.entity.Alert;
import com.bumil.audio_fall_care.domain.alert.repository.AlertRepository;
import com.bumil.audio_fall_care.domain.alert.service.AlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    @Override
    public void markAsRead(Long alertId, Long userId) {
        alertRepository.findByIdAndUserId(alertId, userId)
                .ifPresent(Alert::markAsRead);
    }
}
