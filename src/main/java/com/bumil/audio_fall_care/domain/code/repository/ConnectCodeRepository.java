package com.bumil.audio_fall_care.domain.code.repository;

import com.bumil.audio_fall_care.domain.code.entity.ConnectCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConnectCodeRepository extends JpaRepository<ConnectCode, Long> {
    Optional<ConnectCode> findByCode(String code);
    Optional<ConnectCode> findByUserId(Long userId);
}
