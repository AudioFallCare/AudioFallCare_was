package com.bumil.audio_fall_care.domain.recorder.service;

import com.bumil.audio_fall_care.domain.history.repository.FallHistoryRepository;
import com.bumil.audio_fall_care.domain.recorder.dto.RecorderRegisterRequest;
import com.bumil.audio_fall_care.domain.recorder.dto.RecorderResponse;
import com.bumil.audio_fall_care.domain.recorder.dto.RecorderUpdateRequest;
import com.bumil.audio_fall_care.domain.recorder.entity.Recorder;
import com.bumil.audio_fall_care.domain.recorder.entity.RecorderStatus;
import com.bumil.audio_fall_care.domain.recorder.repository.RecorderRepository;
import com.bumil.audio_fall_care.domain.user.entity.User;
import com.bumil.audio_fall_care.domain.user.repository.UserRepository;
import com.bumil.audio_fall_care.global.common.BusinessException;
import com.bumil.audio_fall_care.global.common.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RecorderServiceTest {

    @InjectMocks
    private RecorderService recorderService;

    @Mock
    private RecorderRepository recorderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FallHistoryRepository fallHistoryRepository;

    private User createUser(Long id) {
        User user = User.builder()
                .username("testuser")
                .password("password")
                .code("ABC123")
                .build();
        setId(user, id);
        return user;
    }

    private void setId(Object entity, Long id) {
        try {
            Field idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Recorder createRecorder(Long id, User user) {
        Recorder recorder = Recorder.builder()
                .user(user)
                .deviceName("거실 리코더")
                .status(RecorderStatus.CONNECTED)
                .build();
        setId(recorder, id);
        return recorder;
    }

    @Nested
    @DisplayName("리코더 등록")
    class RegisterRecorder {

        @Test
        @DisplayName("유효한 연결 코드로 리코더 등록 성공")
        void success() {
            User user = createUser(1L);

            given(userRepository.findByCode("ABC123"))
                    .willReturn(Optional.of(user));
            given(recorderRepository.save(any(Recorder.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            RecorderResponse response = recorderService.registerRecorder(
                    new RecorderRegisterRequest("ABC123"));

            assertThat(response).isNotNull();
            assertThat(response.status()).isEqualTo(RecorderStatus.CONNECTED);
            verify(recorderRepository).save(any(Recorder.class));
        }

        @Test
        @DisplayName("존재하지 않는 연결 코드 - CODE_NOT_FOUND")
        void codeNotFound() {
            given(userRepository.findByCode("INVALID"))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() ->
                    recorderService.registerRecorder(new RecorderRegisterRequest("INVALID")))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.CODE_NOT_FOUND));
        }
    }

    @Nested
    @DisplayName("리코더 정보 수정")
    class UpdateRecorder {

        @Test
        @DisplayName("디바이스 이름 수정 성공")
        void success() {
            User user = createUser(1L);
            Recorder recorder = createRecorder(1L, user);

            given(recorderRepository.findById(1L))
                    .willReturn(Optional.of(recorder));

            RecorderResponse response = recorderService.updateRecorder(
                    user.getId(), 1L, new RecorderUpdateRequest("안방 리코더"));

            assertThat(response.deviceName()).isEqualTo("안방 리코더");
            assertThat(recorder.getDeviceName()).isEqualTo("안방 리코더");
        }

        @Test
        @DisplayName("존재하지 않는 리코더 - RECORDER_NOT_FOUND")
        void recorderNotFound() {
            given(recorderRepository.findById(999L))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() ->
                    recorderService.updateRecorder(1L, 999L, new RecorderUpdateRequest("이름")))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.RECORDER_NOT_FOUND));
        }

        @Test
        @DisplayName("다른 사용자의 리코더 수정 시도 - RECORDER_NOT_OWNED")
        void notOwned() {
            User owner = createUser(1L);
            Recorder recorder = createRecorder(1L, owner);

            given(recorderRepository.findById(1L))
                    .willReturn(Optional.of(recorder));

            assertThatThrownBy(() ->
                    recorderService.updateRecorder(999L, 1L, new RecorderUpdateRequest("이름")))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.RECORDER_NOT_OWNED));
        }
    }
}
