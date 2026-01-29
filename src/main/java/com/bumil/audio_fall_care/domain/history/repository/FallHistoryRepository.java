package com.bumil.audio_fall_care.domain.history.repository;

import com.bumil.audio_fall_care.domain.history.entity.FallHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FallHistoryRepository extends JpaRepository<FallHistory, Long> {
    List<FallHistory> findByUserIdOrderByDetectedAtDesc(Long userId);
}
