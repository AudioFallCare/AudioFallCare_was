package com.bumil.audio_fall_care.domain.fcm.service;

import com.bumil.audio_fall_care.domain.fcm.dto.FcmTokenRequest;
import com.bumil.audio_fall_care.domain.fcm.entity.FcmToken;

public interface FcmTokenServiceInterface {

    void saveToken(Long userId, FcmTokenRequest fcmTokenRequest);
    FcmToken findByUserId(Long userId);
    void deleteToken(FcmToken token);
}
