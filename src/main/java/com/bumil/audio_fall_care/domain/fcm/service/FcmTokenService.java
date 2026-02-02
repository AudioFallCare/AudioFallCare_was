package com.bumil.audio_fall_care.domain.fcm.service;

import com.bumil.audio_fall_care.domain.fcm.dto.FcmTokenRequest;
import com.bumil.audio_fall_care.domain.fcm.entity.FcmToken;
import com.bumil.audio_fall_care.domain.fcm.repository.FcmTokenRepository;
import com.bumil.audio_fall_care.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FcmTokenService implements FcmTokenServiceInterface {

    private final FcmTokenRepository fcmTokenRepository;
    private final UserService userService;

    // FCM 토큰 등록, 갱신
    @Transactional
    @Override
    public void saveOrUpdateToken(Long userId, FcmTokenRequest request) {
        FcmToken token =
                fcmTokenRepository.findByUserIdAndDeviceInfo(userId, request.deviceInfo())
                        .orElseGet(() -> FcmToken.builder()
                                .user(userService.findById(userId))
                                .deviceInfo(request.deviceInfo())
                                .build());

        token.updateToken(request.token());
        fcmTokenRepository.save(token);

        log.info("FCM Token 등록: userId = {}", userId);
    }

    @Override
    public List<FcmToken> findAllByUserId(Long userId) {
        return fcmTokenRepository.findAllByUserId(userId);
    }

    @Transactional
    @Override
    public void deleteToken(FcmToken token) {
        fcmTokenRepository.delete(token);
    }

    @Transactional
    @Override
    public void deleteToken(Long userId, String deviceInfo) {
        fcmTokenRepository.findByUserIdAndDeviceInfo(userId, deviceInfo)
                .ifPresentOrElse(
                        fcmTokenRepository::delete,
                        () -> log.warn("[FCM] 삭제할 토큰이 존재하지 않음: userId = {}, deviceInfo = {}", userId, deviceInfo)
                );
    }
}
