package com.bumil.audio_fall_care.domain.alert.repository;

import com.bumil.audio_fall_care.domain.alert.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<Alert> findByIdAndUserId(Long alertId, Long userId);
    long countByUserIdAndIsReadFalse(Long userId);
}
