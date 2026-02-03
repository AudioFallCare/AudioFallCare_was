package com.bumil.audio_fall_care.domain.user.service;

import com.bumil.audio_fall_care.domain.auth.dto.request.SignUpRequest;
import com.bumil.audio_fall_care.domain.user.entity.User;
import com.bumil.audio_fall_care.domain.user.repository.UserRepository;
import com.bumil.audio_fall_care.domain.user.service.serviceImpl.UserServiceImpl;
import com.bumil.audio_fall_care.global.common.BusinessException;
import com.bumil.audio_fall_care.global.common.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

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
    @DisplayName("사용자 조회")
    class FindUser {

        @Test
        @DisplayName("username으로 조회 성공")
        void findByUsername() {
            User user = User.builder().username("testuser").password("encoded").build();
            setId(user, 1L);

            given(userRepository.findByUsername("testuser")).willReturn(Optional.of(user));

            User result = userService.findByUsername("testuser");

            assertThat(result.getUsername()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("존재하지 않는 username - USER_NOT_FOUND")
        void findByUsernameNotFound() {
            given(userRepository.findByUsername("unknown")).willReturn(Optional.empty());

            assertThatThrownBy(() -> userService.findByUsername("unknown"))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.USER_NOT_FOUND));
        }

        @Test
        @DisplayName("ID로 조회 성공")
        void findById() {
            User user = User.builder().username("testuser").password("encoded").build();
            setId(user, 1L);

            given(userRepository.findById(1L)).willReturn(Optional.of(user));

            User result = userService.findById(1L);

            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("존재하지 않는 ID - USER_NOT_FOUND")
        void findByIdNotFound() {
            given(userRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> userService.findById(999L))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.USER_NOT_FOUND));
        }
    }

    @Nested
    @DisplayName("회원가입")
    class SignUp {

        @Test
        @DisplayName("회원가입 성공")
        void success() {
            SignUpRequest request = new SignUpRequest(
                    "newuser", "password1", "password1",
                    "12345", "서울시 강남구", "101호"
            );

            given(userRepository.existsByUsername("newuser")).willReturn(false);
            given(userRepository.existsByCode(anyString())).willReturn(false);
            given(passwordEncoder.encode("password1")).willReturn("encodedPw");
            given(userRepository.save(any(User.class))).willAnswer(invocation -> {
                User user = invocation.getArgument(0);
                setId(user, 1L);
                return user;
            });

            Long userId = userService.signUp(request);

            assertThat(userId).isEqualTo(1L);
        }

        @Test
        @DisplayName("비밀번호 불일치 - MISMATCHED_PASSWORD")
        void mismatchedPassword() {
            SignUpRequest request = new SignUpRequest(
                    "newuser", "password1", "different",
                    "12345", "서울시 강남구", null
            );

            assertThatThrownBy(() -> userService.signUp(request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.MISMATCHED_PASSWORD));
        }

        @Test
        @DisplayName("중복 아이디 - DUPLICATED_USERNAME")
        void duplicatedUsername() {
            SignUpRequest request = new SignUpRequest(
                    "existing", "password1", "password1",
                    "12345", "서울시 강남구", null
            );

            given(userRepository.existsByUsername("existing")).willReturn(true);

            assertThatThrownBy(() -> userService.signUp(request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.DUPLICATED_USERNAME));
        }
    }
}
