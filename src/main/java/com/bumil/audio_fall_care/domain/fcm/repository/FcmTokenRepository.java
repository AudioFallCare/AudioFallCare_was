package com.bumil.audio_fall_care.domain.fcm.repository;

import com.bumil.audio_fall_care.domain.fcm.entity.FcmToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {

    Optional<FcmToken> findByUserId(Long userId);
}
