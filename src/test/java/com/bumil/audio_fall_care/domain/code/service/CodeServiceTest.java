package com.bumil.audio_fall_care.domain.code.service;

import com.bumil.audio_fall_care.domain.code.dto.response.CodeGenerateResponse;
import com.bumil.audio_fall_care.domain.code.dto.response.CodeVerifyResponse;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CodeServiceTest {

    @InjectMocks
    private CodeService codeService;

    @Mock
    private UserRepository userRepository;

    private User createUser(Long id, String code) {
        User user = User.builder().username("testuser").password("password").code(code).build();
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
    @DisplayName("코드 조회")
    class GenerateCode {

        @Test
        @DisplayName("기존 User.code 반환")
        void returnsExistingCode() {
            User user = createUser(1L, "ABC123");

            given(userRepository.findById(1L)).willReturn(Optional.of(user));

            CodeGenerateResponse response = codeService.generateCode(1L);

            assertThat(response.code()).isEqualTo("ABC123");
        }

        @Test
        @DisplayName("존재하지 않는 사용자 - USER_NOT_FOUND")
        void userNotFound() {
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
        @DisplayName("새 코드로 변경")
        void regeneratesCode() {
            User user = createUser(1L, "OLD123");

            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(userRepository.existsByCode(anyString())).willReturn(false);

            CodeGenerateResponse response = codeService.regenerateCode(1L);

            assertThat(response.code()).hasSize(6);
            assertThat(response.code()).isNotEqualTo("OLD123");
            assertThat(user.getCode()).isEqualTo(response.code());
        }

        @Test
        @DisplayName("존재하지 않는 사용자 - USER_NOT_FOUND")
        void userNotFound() {
            given(userRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> codeService.regenerateCode(999L))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.USER_NOT_FOUND));
        }
    }

    @Nested
    @DisplayName("코드 검증")
    class VerifyCode {

        @Test
        @DisplayName("유효한 코드 검증 성공")
        void success() {
            User user = createUser(1L, "VALID1");

            given(userRepository.findByCode("VALID1")).willReturn(Optional.of(user));

            CodeVerifyResponse response = codeService.verifyCode("VALID1");

            assertThat(response.userId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("존재하지 않는 코드 - CODE_NOT_FOUND")
        void codeNotFound() {
            given(userRepository.findByCode("NOPE")).willReturn(Optional.empty());

            assertThatThrownBy(() -> codeService.verifyCode("NOPE"))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.CODE_NOT_FOUND));
        }
    }
}
