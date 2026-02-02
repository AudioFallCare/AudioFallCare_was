package com.bumil.audio_fall_care.domain.fcm.service;

import com.bumil.audio_fall_care.domain.fcm.entity.FcmToken;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "firebase.enabled", havingValue = "true")
public class FcmService implements FcmServiceInterface {

    private final FcmTokenServiceInterface fcmTokenService;
    private final FirebaseMessaging firebaseMessaging;

    @Override
    public void sendToUser(Long userId, String title, String body) {
        List<FcmToken> tokens = fcmTokenService.findAllByUserId(userId);

        for (FcmToken token : tokens) {
            Message message = Message.builder()
                    .setToken(token.getToken())
                    .setWebpushConfig(WebpushConfig.builder()
                            .setNotification(WebpushNotification.builder()
                                    .setTitle(title)
                                    .setBody(body)
                                    .setIcon("/icons/alert-icon.png")
                                    .setVibrate(new int[]{200, 100, 200})
                                    .build())
                            .build())
                    .build();

            try {
                firebaseMessaging.send(message);
                log.info("[FCM] 알림 전송 성공: userId: {}", userId);
            } catch (FirebaseMessagingException e) {
                log.error("[FCM] 알림 전송 실패: userId: {}, errorCode: {}, message: {}",
                        userId, e.getMessagingErrorCode(), e.getMessage());

                if (isTokenInvalid(e)) {
                    fcmTokenService.deleteToken(token);
                    log.warn("[FCM] 만료/무효 토큰 삭제: userId={}, errorCode={}",
                            userId, e.getMessagingErrorCode());
                }
            } catch (Exception e) {
                log.error("[FCM] 예상치 못한 오류 발생: userId: {}", userId, e);
            }
        }
    }

    // 토큰 유효성 검사
    private boolean isTokenInvalid(FirebaseMessagingException e) {
        return e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED
                || e.getMessagingErrorCode() == MessagingErrorCode.INVALID_ARGUMENT;
    }
}
