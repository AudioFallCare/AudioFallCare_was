package com.bumil.audio_fall_care.domain.code.service;

import com.bumil.audio_fall_care.domain.code.dto.response.CodeGenerateResponse;
import com.bumil.audio_fall_care.domain.code.dto.response.CodeVerifyResponse;
import com.bumil.audio_fall_care.domain.code.entity.ConnectCode;
import com.bumil.audio_fall_care.domain.code.repository.ConnectCodeRepository;
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
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CodeServiceTest {

    @InjectMocks
    private CodeService codeService;

    @Mock
    private ConnectCodeRepository connectCodeRepository;

    @Mock
    private UserRepository userRepository;

    private User createUser(Long id) {
        User user = User.builder().username("testuser").password("password").build();
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

    @Nested
    @DisplayName("코드 생성")
    class GenerateCode {

        @Test
        @DisplayName("기존 코드가 있으면 그대로 반환")
        void returnsExistingCode() {
            User user = createUser(1L);
            ConnectCode existing = ConnectCode.builder()
                    .user(user).code("EXIST123").used(false).build();
            setId(existing, 10L);

            given(connectCodeRepository.findByUserId(1L)).willReturn(Optional.of(existing));

            CodeGenerateResponse response = codeService.generateCode(1L);

            assertThat(response.code()).isEqualTo("EXIST123");
            assertThat(response.id()).isEqualTo(10L);
        }

        @Test
        @DisplayName("기존 코드가 없으면 새로 생성")
        void createsNewCode() {
            User user = createUser(1L);

            given(connectCodeRepository.findByUserId(1L)).willReturn(Optional.empty());
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(connectCodeRepository.save(any(ConnectCode.class)))
                    .willAnswer(invocation -> {
                        ConnectCode code = invocation.getArgument(0);
                        setId(code, 20L);
                        return code;
                    });

            CodeGenerateResponse response = codeService.generateCode(1L);

            assertThat(response.code()).hasSize(8);
            assertThat(response.id()).isEqualTo(20L);
            verify(connectCodeRepository).save(any(ConnectCode.class));
        }

        @Test
        @DisplayName("존재하지 않는 사용자 - USER_NOT_FOUND")
        void userNotFound() {
            given(connectCodeRepository.findByUserId(999L)).willReturn(Optional.empty());
            given(userRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> codeService.generateCode(999L))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.USER_NOT_FOUND));
        }
    }

    @Nested
    @DisplayName("코드 재생성")
    class RegenerateCode {

        @Test
        @DisplayName("기존 코드 삭제 후 새 코드 생성")
        void deletesOldAndCreatesNew() {
            User user = createUser(1L);
            ConnectCode existing = ConnectCode.builder()
                    .user(user).code("OLD12345").used(false).build();

            given(connectCodeRepository.findByUserId(1L)).willReturn(Optional.of(existing));
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(connectCodeRepository.save(any(ConnectCode.class)))
                    .willAnswer(invocation -> {
                        ConnectCode code = invocation.getArgument(0);
                        setId(code, 30L);
                        return code;
                    });

            CodeGenerateResponse response = codeService.regenerateCode(1L);

            verify(connectCodeRepository).delete(existing);
            assertThat(response.code()).hasSize(8);
            assertThat(response.code()).isNotEqualTo("OLD12345");
        }
    }

    @Nested
    @DisplayName("코드 검증")
    class VerifyCode {

        @Test
        @DisplayName("유효한 코드 검증 성공")
        void success() {
            User user = createUser(1L);
            ConnectCode code = ConnectCode.builder()
                    .user(user).code("VALID123").used(false).build();
            setId(code, 5L);

            given(connectCodeRepository.findByCode("VALID123")).willReturn(Optional.of(code));

            CodeVerifyResponse response = codeService.verifyCode("VALID123");

            assertThat(response.userId()).isEqualTo(1L);
            assertThat(response.codeId()).isEqualTo(5L);
        }

        @Test
        @DisplayName("존재하지 않는 코드 - CODE_NOT_FOUND")
        void codeNotFound() {
            given(connectCodeRepository.findByCode("NOPE")).willReturn(Optional.empty());

            assertThatThrownBy(() -> codeService.verifyCode("NOPE"))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.CODE_NOT_FOUND));
        }

        @Test
        @DisplayName("만료된 코드 - CODE_EXPIRED")
        void codeExpired() {
            User user = createUser(1L);
            ConnectCode code = ConnectCode.builder()
                    .user(user).code("EXPRD123").used(false)
                    .expiresAt(LocalDateTime.now().minusDays(1)).build();

            given(connectCodeRepository.findByCode("EXPRD123")).willReturn(Optional.of(code));

            assertThatThrownBy(() -> codeService.verifyCode("EXPRD123"))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.CODE_EXPIRED));
        }

        @Test
        @DisplayName("이미 사용된 코드 - CODE_ALREADY_USED")
        void codeAlreadyUsed() {
            User user = createUser(1L);
            ConnectCode code = ConnectCode.builder()
                    .user(user).code("USED1234").used(true).build();

            given(connectCodeRepository.findByCode("USED1234")).willReturn(Optional.of(code));

            assertThatThrownBy(() -> codeService.verifyCode("USED1234"))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.CODE_ALREADY_USED));
        }
    }
}
