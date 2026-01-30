package com.bumil.audio_fall_care.domain.fcm.service;

import com.bumil.audio_fall_care.domain.fcm.dto.FcmTokenRequest;
import com.bumil.audio_fall_care.domain.fcm.entity.FcmToken;
import com.bumil.audio_fall_care.domain.fcm.repository.FcmTokenRepository;
import com.bumil.audio_fall_care.domain.user.entity.User;
import com.bumil.audio_fall_care.domain.user.service.UserService;
import com.bumil.audio_fall_care.global.common.BusinessException;
import com.bumil.audio_fall_care.global.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FcmTokenService implements FcmTokenServiceInterface {

    private final FcmTokenRepository fcmTokenRepository;
    private final UserService userService;

    // FCM 토큰 등록
    @Override
    public void saveToken(Long userId, FcmTokenRequest request) {
        User user = userService.findById(userId);

        FcmToken newFcmToken = FcmToken.builder()
                .user(user)
                .token(request.token())
                .deviceInfo(request.deviceInfo())
                .build();

        fcmTokenRepository.save(newFcmToken);

        log.info("FCM Token 생성: userId = {}", userId);
    }

    @Override
    public FcmToken findByUserId(Long userId) {
        return fcmTokenRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FCM_TOKEN_NOT_FOUND));
    }

    @Override
    public void deleteToken(FcmToken token) {
        fcmTokenRepository.delete(token);
    }
}
