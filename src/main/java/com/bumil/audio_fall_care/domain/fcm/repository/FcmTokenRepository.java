package com.bumil.audio_fall_care.domain.fcm.repository;

import com.bumil.audio_fall_care.domain.fcm.entity.FcmToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {

    List<FcmToken> findAllByUserId(Long userId);
    Optional<FcmToken> findByUserIdAndDeviceInfo(Long userId, String deviceInfo);
}
