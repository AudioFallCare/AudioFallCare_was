package com.bumil.audio_fall_care.domain.recorder.repository;

import com.bumil.audio_fall_care.domain.recorder.entity.Recorder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RecorderRepository extends JpaRepository<Recorder, Long> {
    Optional<Recorder> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
}
