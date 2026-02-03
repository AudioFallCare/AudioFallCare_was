package com.bumil.audio_fall_care.domain.alert.service;

import com.bumil.audio_fall_care.domain.alert.dto.response.AlertResponse;
import com.bumil.audio_fall_care.domain.alert.entity.Alert;
import com.bumil.audio_fall_care.domain.alert.entity.AlertType;
import com.bumil.audio_fall_care.domain.alert.repository.AlertRepository;
import com.bumil.audio_fall_care.domain.alert.service.serviceImpl.AlertServiceImpl;
import com.bumil.audio_fall_care.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @InjectMocks
    private AlertServiceImpl alertService;

    @Mock
    private AlertRepository alertRepository;

    private void setId(Object entity, Long id) {
        try {
            Field idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private User createUser(Long id) {
        User user = User.builder().username("testuser").password("password").build();
        setId(user, id);
        return user;
    }

    private Alert createAlert(Long id, User user, String message, boolean isRead) {
        Alert alert = Alert.builder()
                .user(user)
                .type(AlertType.FALL)
                .message(message)
                .build();
        setId(alert, id);
        if (isRead) alert.markAsRead();
        return alert;
    }

    @Test
    @DisplayName("사용자의 알림 목록 조회")
    void findAllByUserId() {
        User user = createUser(1L);
        List<Alert> alerts = List.of(
                createAlert(1L, user, "낙상 감지 1", false),
                createAlert(2L, user, "낙상 감지 2", true)
        );

        given(alertRepository.findByUserIdOrderByCreatedAtDesc(1L)).willReturn(alerts);

        List<AlertResponse> result = alertService.findAllByUserId(1L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).message()).isEqualTo("낙상 감지 1");
        assertThat(result.get(0).isRead()).isFalse();
        assertThat(result.get(1).isRead()).isTrue();
    }

    @Test
    @DisplayName("빈 알림 목록 조회")
    void findAllByUserIdEmpty() {
        given(alertRepository.findByUserIdOrderByCreatedAtDesc(1L)).willReturn(List.of());

        List<AlertResponse> result = alertService.findAllByUserId(1L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("읽지 않은 알림 개수 조회")
    void countUnreadAlerts() {
        given(alertRepository.countByUserIdAndIsReadFalse(1L)).willReturn(3L);

        long count = alertService.countUnreadAlerts(1L);

        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("알림 읽음 처리")
    void markAsRead() {
        User user = createUser(1L);
        Alert alert = createAlert(10L, user, "낙상 감지", false);

        given(alertRepository.findByIdAndUserId(10L, 1L)).willReturn(Optional.of(alert));

        alertService.markAsRead(10L, 1L);

        assertThat(alert.getIsRead()).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 알림 읽음 처리 - 예외 없이 무시")
    void markAsReadNotFound() {
        given(alertRepository.findByIdAndUserId(999L, 1L)).willReturn(Optional.empty());

        alertService.markAsRead(999L, 1L);

        // No exception thrown
    }
}
