package com.bumil.audio_fall_care.domain.internal.service;

import com.bumil.audio_fall_care.domain.alert.entity.Alert;
import com.bumil.audio_fall_care.domain.alert.entity.AlertType;
import com.bumil.audio_fall_care.domain.alert.repository.AlertRepository;
import com.bumil.audio_fall_care.domain.fcm.service.FcmServiceInterface;
import com.bumil.audio_fall_care.domain.history.entity.FallHistory;
import com.bumil.audio_fall_care.domain.history.repository.FallHistoryRepository;
import com.bumil.audio_fall_care.domain.internal.dto.FallDetectionRequest;
import com.bumil.audio_fall_care.domain.internal.dto.FallDetectionResponse;
import com.bumil.audio_fall_care.domain.recorder.entity.Recorder;
import com.bumil.audio_fall_care.domain.recorder.repository.RecorderRepository;
import com.bumil.audio_fall_care.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class InternalService {

    private final RecorderRepository recorderRepository;
    private final FallHistoryRepository fallHistoryRepository;
    private final AlertRepository alertRepository;
    private final FcmServiceInterface fcmService;

    /**
     * AI 서버에서 낙상 감지 결과를 수신하여 처리한다.
     *
     * 1. Recorder 조회
     * 2. FallHistory 저장
     * 3. Alert 생성
     * 4. FCM 푸시 알림 전송
     */
    @Transactional
    public FallDetectionResponse processFallDetection(FallDetectionRequest request) {
        // 1. Recorder 조회
        Recorder recorder = recorderRepository.findById(request.recorderId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "존재하지 않는 리코더입니다: " + request.recorderId()));

        User user = recorder.getUser();
        LocalDateTime detectedAt = request.detectedAt() != null ? request.detectedAt() : LocalDateTime.now();

        // 2. FallHistory 저장
        FallHistory history = FallHistory.builder()
                .recorder(recorder)
                .user(user)
                .confidence(request.confidence())
                .soundType(request.soundType())
                .detectedAt(detectedAt)
                .build();
        fallHistoryRepository.save(history);

        // 3. Alert 생성
        String alertMessage = String.format(
                "[낙상 감지] %s에서 낙상이 감지되었습니다. (신뢰도: %.0f%%, 소리 유형: %s)",
                recorder.getDeviceName(),
                request.confidence() * 100,
                request.soundType()
        );

        Alert alert = Alert.builder()
                .user(user)
                .type(AlertType.FALL)
                .message(alertMessage)
                .build();
        alertRepository.save(alert);

        // 4. FCM 푸시 알림 전송
        try {
            fcmService.sendToUser(user.getId(), "낙상 감지 알림", alertMessage);
        } catch (Exception e) {
            log.error("FCM 알림 전송 실패: userId={}, error={}", user.getId(), e.getMessage());
        }

        log.info("낙상 감지 처리 완료: recorderId={}, userId={}, confidence={}, soundType={}",
                recorder.getId(), user.getId(), request.confidence(), request.soundType());

        return new FallDetectionResponse(
                history.getId(),
                alert.getId(),
                recorder.getId(),
                user.getId(),
                request.confidence(),
                request.soundType(),
                detectedAt
        );
    }
}
