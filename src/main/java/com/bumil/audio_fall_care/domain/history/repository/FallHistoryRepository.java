package com.bumil.audio_fall_care.domain.history.repository;

import com.bumil.audio_fall_care.domain.history.entity.FallHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface FallHistoryRepository extends JpaRepository<FallHistory, Long> {
    List<FallHistory> findByUserIdOrderByDetectedAtDesc(Long userId);

    long countByUserId(Long userId);

    long countByUserIdAndDetectedAtAfter(Long userId, LocalDateTime after);

    @Query("SELECT COALESCE(AVG(h.confidence), 0) FROM FallHistory h WHERE h.user.id = :userId")
    double findAverageConfidenceByUserId(@Param("userId") Long userId);

    void deleteAllByRecorderId(Long recorderId);
}
