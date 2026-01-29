package com.bumil.audio_fall_care.domain.fcm.service;

public interface FcmServiceInterface {

    /**
     * 특정 사용자에게 푸시 알림을 전송한다.
     *
     * @param userId 대상 사용자 ID
     * @param title  알림 제목
     * @param body   알림 본문
     */
    void sendToUser(Long userId, String title, String body);
}
