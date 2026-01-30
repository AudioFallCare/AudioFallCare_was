package com.bumil.audio_fall_care.domain.fcm.service;

import com.bumil.audio_fall_care.domain.fcm.dto.FcmTokenRequest;

public interface FcmTokenServiceInterface {

    void saveToken(Long userId, FcmTokenRequest fcmTokenRequest);
}
