package com.bumil.audio_fall_care.domain.fcm.service;

import com.bumil.audio_fall_care.domain.fcm.dto.FcmTokenRequest;
import com.bumil.audio_fall_care.domain.fcm.entity.FcmToken;

import java.util.List;

public interface FcmTokenServiceInterface {

    void saveOrUpdateToken(Long userId, FcmTokenRequest fcmTokenRequest);
    List<FcmToken> findAllByUserId(Long userId);
    void deleteToken(FcmToken token);
}
