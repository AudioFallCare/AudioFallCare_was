package com.bumil.audio_fall_care.domain.fcm.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Firebase가 설정되지 않았을 때 사용되는 No-Op 구현체.
 * 실제 푸시 알림을 보내지 않고 로그만 남긴다.
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "firebase.enabled", havingValue = "false", matchIfMissing = true)
public class NoOpFcmService implements FcmServiceInterface {

    @Override
    public void sendToUser(Long userId, String title, String body) {
        log.info("[NoOp FCM] userId={}, title={}, body={}", userId, title, body);
    }
}
