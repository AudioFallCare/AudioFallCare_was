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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecorderService {

    private final RecorderRepository recorderRepository;
    private final ConnectCodeRepository connectCodeRepository;

    @Transactional
    public RecorderResponse registerRecorder(RecorderRegisterRequest request) {
        ConnectCode connectCode = connectCodeRepository.findByCode(request.code())
                .orElseThrow(() -> new BusinessException(ErrorCode.CODE_NOT_FOUND));

        if (connectCode.getUsed()) {
            throw new BusinessException(ErrorCode.CODE_ALREADY_USED);
        }

        if (connectCode.getExpiresAt() != null
                && connectCode.getExpiresAt().isBefore(java.time.LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.CODE_EXPIRED);
        }

        User user = connectCode.getUser();

        Recorder recorder = Recorder.builder()
                .user(user)
                .status(RecorderStatus.CONNECTED)
                .build();
        recorderRepository.save(recorder);

        connectCode.markAsUsed();

        log.info("리코더 등록 완료: recorderId={}, userId={}",
                recorder.getId(), user.getId());

        return RecorderResponse.from(recorder);
    }

    public List<RecorderResponse> getRecorders(Long userId) {
        return recorderRepository.findByUserId(userId).stream()
                .map(RecorderResponse::from)
                .toList();
    }

    @Transactional
    public RecorderResponse updateRecorder(Long userId, Long recorderId, RecorderUpdateRequest request) {
        Recorder recorder = recorderRepository.findById(recorderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RECORDER_NOT_FOUND));

        if (!recorder.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.RECORDER_NOT_OWNED);
        }

        recorder.updateDeviceName(request.deviceName());

        return RecorderResponse.from(recorder);
    }

    @Transactional
    public void deleteRecorder(Long userId, Long recorderId) {
        Recorder recorder = recorderRepository.findById(recorderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RECORDER_NOT_FOUND));

        if (!recorder.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.RECORDER_NOT_OWNED);
        }

        recorderRepository.delete(recorder);
    }

    public RecorderResponse getRecorderStatus(Long userId, Long recorderId) {
        Recorder recorder = recorderRepository.findById(recorderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RECORDER_NOT_FOUND));

        if (!recorder.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.RECORDER_NOT_OWNED);
        }

        return RecorderResponse.from(recorder);
    }
}
