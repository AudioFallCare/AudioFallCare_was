package com.bumil.audio_fall_care.domain.history.service;

import com.bumil.audio_fall_care.domain.history.dto.FallHistoryResponse;
import com.bumil.audio_fall_care.domain.history.dto.FallHistoryStatsResponse;
import com.bumil.audio_fall_care.domain.history.entity.FallHistory;
import com.bumil.audio_fall_care.domain.history.repository.FallHistoryRepository;
import com.bumil.audio_fall_care.domain.recorder.entity.Recorder;
import com.bumil.audio_fall_care.domain.recorder.entity.RecorderStatus;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FallHistoryServiceTest {

    @InjectMocks
    private FallHistoryService fallHistoryService;

    @Mock
    private FallHistoryRepository fallHistoryRepository;

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

    private Recorder createRecorder(Long id, User user) {
        Recorder recorder = Recorder.builder()
                .user(user).deviceName("거실").status(RecorderStatus.CONNECTED).build();
        setId(recorder, id);
        return recorder;
    }

    private FallHistory createHistory(Long id, User user, Recorder recorder) {
        FallHistory history = FallHistory.builder()
                .user(user).recorder(recorder)
                .confidence(0.92).soundType("thud")
                .detectedAt(LocalDateTime.now())
                .build();
        setId(history, id);
        return history;
    }

    @Test
    @DisplayName("낙상 이력 목록 조회")
    void getHistories() {
        User user = createUser(1L);
        Recorder recorder = createRecorder(1L, user);
        List<FallHistory> histories = List.of(
                createHistory(1L, user, recorder),
                createHistory(2L, user, recorder)
        );

        given(fallHistoryRepository.findByUserIdOrderByDetectedAtDesc(1L)).willReturn(histories);

        List<FallHistoryResponse> result = fallHistoryService.getHistories(1L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).deviceName()).isEqualTo("거실");
    }

    @Nested
    @DisplayName("낙상 이력 상세 조회")
    class GetHistory {

        @Test
        @DisplayName("조회 성공")
        void success() {
            User user = createUser(1L);
            Recorder recorder = createRecorder(1L, user);
            FallHistory history = createHistory(10L, user, recorder);

            given(fallHistoryRepository.findById(10L)).willReturn(Optional.of(history));

            FallHistoryResponse result = fallHistoryService.getHistory(1L, 10L);

            assertThat(result.id()).isEqualTo(10L);
            assertThat(result.confidence()).isEqualTo(0.92);
        }

        @Test
        @DisplayName("존재하지 않는 이력 - FALL_HISTORY_NOT_FOUND")
        void notFound() {
            given(fallHistoryRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> fallHistoryService.getHistory(1L, 999L))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.FALL_HISTORY_NOT_FOUND));
        }

        @Test
        @DisplayName("다른 사용자의 이력 조회 - FALL_HISTORY_NOT_OWNED")
        void notOwned() {
            User owner = createUser(1L);
            Recorder recorder = createRecorder(1L, owner);
            FallHistory history = createHistory(10L, owner, recorder);

            given(fallHistoryRepository.findById(10L)).willReturn(Optional.of(history));

            assertThatThrownBy(() -> fallHistoryService.getHistory(999L, 10L))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.FALL_HISTORY_NOT_OWNED));
        }
    }

    @Nested
    @DisplayName("낙상 이력 삭제")
    class DeleteHistory {

        @Test
        @DisplayName("삭제 성공")
        void success() {
            User user = createUser(1L);
            Recorder recorder = createRecorder(1L, user);
            FallHistory history = createHistory(10L, user, recorder);

            given(fallHistoryRepository.findById(10L)).willReturn(Optional.of(history));

            fallHistoryService.deleteHistory(1L, 10L);

            verify(fallHistoryRepository).delete(history);
        }

        @Test
        @DisplayName("다른 사용자의 이력 삭제 - FALL_HISTORY_NOT_OWNED")
        void notOwned() {
            User owner = createUser(1L);
            Recorder recorder = createRecorder(1L, owner);
            FallHistory history = createHistory(10L, owner, recorder);

            given(fallHistoryRepository.findById(10L)).willReturn(Optional.of(history));

            assertThatThrownBy(() -> fallHistoryService.deleteHistory(999L, 10L))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.FALL_HISTORY_NOT_OWNED));
        }
    }

    @Test
    @DisplayName("통계 조회")
    void getStats() {
        given(fallHistoryRepository.countByUserId(1L)).willReturn(10L);
        given(fallHistoryRepository.countByUserIdAndDetectedAtAfter(eq(1L), any(LocalDateTime.class)))
                .willReturn(3L, 7L);
        given(fallHistoryRepository.findAverageConfidenceByUserId(1L)).willReturn(0.88);

        FallHistoryStatsResponse stats = fallHistoryService.getStats(1L);

        assertThat(stats.totalCount()).isEqualTo(10);
        assertThat(stats.recentWeekCount()).isEqualTo(3);
        assertThat(stats.recentMonthCount()).isEqualTo(7);
        assertThat(stats.averageConfidence()).isEqualTo(0.88);
    }
}
