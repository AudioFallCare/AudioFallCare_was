package com.bumil.audio_fall_care.domain.internal.service;

import com.bumil.audio_fall_care.domain.alert.entity.Alert;
import com.bumil.audio_fall_care.domain.alert.repository.AlertRepository;
import com.bumil.audio_fall_care.domain.fcm.service.FcmServiceInterface;
import com.bumil.audio_fall_care.domain.history.entity.FallHistory;
import com.bumil.audio_fall_care.domain.history.repository.FallHistoryRepository;
import com.bumil.audio_fall_care.domain.internal.dto.FallDetectionRequest;
import com.bumil.audio_fall_care.domain.internal.dto.FallDetectionResponse;
import com.bumil.audio_fall_care.domain.recorder.entity.Recorder;
import com.bumil.audio_fall_care.domain.recorder.entity.RecorderStatus;
import com.bumil.audio_fall_care.domain.recorder.repository.RecorderRepository;
import com.bumil.audio_fall_care.domain.user.entity.User;
import com.bumil.audio_fall_care.global.common.BusinessException;
import com.bumil.audio_fall_care.global.common.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InternalServiceTest {

    @InjectMocks
    private InternalService internalService;

    @Mock
    private RecorderRepository recorderRepository;

    @Mock
    private FallHistoryRepository fallHistoryRepository;

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private FcmServiceInterface fcmService;

    private User createUser() {
        return User.builder()
                .username("testuser")
                .password("password")
                .build();
    }

    private Recorder createRecorder(User user) {
        return Recorder.builder()
                .user(user)
                .deviceName("거실 리코더")
                .status(RecorderStatus.CONNECTED)
                .build();
    }

    @Test
    @DisplayName("detectedAt을 전송하면 해당 시간이 사용된다")
    void usesProvidedDetectedAt() {
        User user = createUser();
        Recorder recorder = createRecorder(user);
        LocalDateTime customTime = LocalDateTime.of(2025, 6, 15, 14, 30, 0);

        given(recorderRepository.findById(1L)).willReturn(Optional.of(recorder));
        given(fallHistoryRepository.save(any(FallHistory.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        given(alertRepository.save(any(Alert.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        FallDetectionRequest request = new FallDetectionRequest(1L, 0.95, "thud", customTime);
        FallDetectionResponse response = internalService.processFallDetection(request);

        assertThat(response.detectedAt()).isEqualTo(customTime);
        verify(fallHistoryRepository).save(any(FallHistory.class));
        verify(alertRepository).save(any(Alert.class));
    }

    @Test
    @DisplayName("detectedAt이 null이면 서버 시간이 사용된다")
    void usesServerTimeWhenDetectedAtIsNull() {
        User user = createUser();
        Recorder recorder = createRecorder(user);
        LocalDateTime before = LocalDateTime.now();

        given(recorderRepository.findById(1L)).willReturn(Optional.of(recorder));
        given(fallHistoryRepository.save(any(FallHistory.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        given(alertRepository.save(any(Alert.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        FallDetectionRequest request = new FallDetectionRequest(1L, 0.85, "crash", null);
        FallDetectionResponse response = internalService.processFallDetection(request);

        LocalDateTime after = LocalDateTime.now();
        assertThat(response.detectedAt()).isBetween(before, after);
    }

    @Test
    @DisplayName("존재하지 않는 리코더 ID - BusinessException(RECORDER_NOT_FOUND)")
    void recorderNotFound() {
        given(recorderRepository.findById(999L)).willReturn(Optional.empty());

        FallDetectionRequest request = new FallDetectionRequest(999L, 0.9, "thud", null);

        assertThatThrownBy(() -> internalService.processFallDetection(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.RECORDER_NOT_FOUND));
    }
}
