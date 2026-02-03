package com.bumil.audio_fall_care.domain.recorder.service;

import com.bumil.audio_fall_care.domain.code.entity.ConnectCode;
import com.bumil.audio_fall_care.domain.code.repository.ConnectCodeRepository;
import com.bumil.audio_fall_care.domain.recorder.dto.RecorderRegisterRequest;
import com.bumil.audio_fall_care.domain.recorder.dto.RecorderResponse;
import com.bumil.audio_fall_care.domain.recorder.dto.RecorderUpdateRequest;
import com.bumil.audio_fall_care.domain.recorder.entity.Recorder;
import com.bumil.audio_fall_care.domain.recorder.entity.RecorderStatus;
import com.bumil.audio_fall_care.domain.recorder.repository.RecorderRepository;
import com.bumil.audio_fall_care.domain.user.entity.User;
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
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class   RecorderServiceTest {

    @InjectMocks
    private RecorderService recorderService;

    @Mock
    private RecorderRepository recorderRepository;

    @Mock
    private ConnectCodeRepository connectCodeRepository;

    private User createUser(Long id) {
        User user = User.builder()
                .username("testuser")
                .password("password")
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
        return Recorder.builder()
                .user(user)
                .deviceName("거실 리코더")
                .status(RecorderStatus.CONNECTED)
                .build();
    }

    @Nested
    @DisplayName("리코더 등록")
    class RegisterRecorder {

        @Test
        @DisplayName("유효한 연결 코드로 리코더 등록 성공")
        void success() {
            // given
            User user = createUser(1L);
            ConnectCode connectCode = ConnectCode.builder()
                    .user(user)
                    .code("ABC123")
                    .used(false)
                    .build();

            given(connectCodeRepository.findByCode("ABC123"))
                    .willReturn(Optional.of(connectCode));
            given(recorderRepository.save(any(Recorder.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            RecorderResponse response = recorderService.registerRecorder(
                    new RecorderRegisterRequest("ABC123"));

            // then
            assertThat(response).isNotNull();
            assertThat(response.status()).isEqualTo(RecorderStatus.CONNECTED);
            assertThat(connectCode.getUsed()).isTrue();
            verify(recorderRepository).save(any(Recorder.class));
        }

        @Test
        @DisplayName("존재하지 않는 연결 코드 - CODE_NOT_FOUND")
        void codeNotFound() {
            given(connectCodeRepository.findByCode("INVALID"))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() ->
                    recorderService.registerRecorder(new RecorderRegisterRequest("INVALID")))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.CODE_NOT_FOUND));
        }

        @Test
        @DisplayName("이미 사용된 연결 코드 - CODE_ALREADY_USED")
        void codeAlreadyUsed() {
            User user = createUser(1L);
            ConnectCode connectCode = ConnectCode.builder()
                    .user(user)
                    .code("USED01")
                    .used(true)
                    .build();

            given(connectCodeRepository.findByCode("USED01"))
                    .willReturn(Optional.of(connectCode));

            assertThatThrownBy(() ->
                    recorderService.registerRecorder(new RecorderRegisterRequest("USED01")))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.CODE_ALREADY_USED));
        }

        @Test
        @DisplayName("만료된 연결 코드 - CODE_EXPIRED")
        void codeExpired() {
            User user = createUser(1L);
            ConnectCode connectCode = ConnectCode.builder()
                    .user(user)
                    .code("EXPRD1")
                    .used(false)
                    .expiresAt(LocalDateTime.now().minusDays(1))
                    .build();

            given(connectCodeRepository.findByCode("EXPRD1"))
                    .willReturn(Optional.of(connectCode));

            assertThatThrownBy(() ->
                    recorderService.registerRecorder(new RecorderRegisterRequest("EXPRD1")))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.CODE_EXPIRED));
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
