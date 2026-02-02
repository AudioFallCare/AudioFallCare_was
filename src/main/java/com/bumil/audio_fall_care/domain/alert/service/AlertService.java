package com.bumil.audio_fall_care.domain.alert.service;

import com.bumil.audio_fall_care.domain.alert.dto.response.AlertResponse;

import java.util.List;

public interface AlertService {

    List<AlertResponse> findAllByUserId(Long userId);
    long countUnreadAlerts(Long userId);
    void markAsRead(Long alertId, Long userId);
}
