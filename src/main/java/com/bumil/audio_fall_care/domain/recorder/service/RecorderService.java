package com.bumil.audio_fall_care.domain.recorder.service;

import com.bumil.audio_fall_care.domain.recorder.dto.RecorderResponse;
import com.bumil.audio_fall_care.domain.recorder.entity.Recorder;
import com.bumil.audio_fall_care.domain.recorder.repository.RecorderRepository;
import com.bumil.audio_fall_care.global.common.BusinessException;
import com.bumil.audio_fall_care.global.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecorderService {

    private final RecorderRepository recorderRepository;

    public List<RecorderResponse> getRecorders(Long userId) {
        return recorderRepository.findByUserId(userId).stream()
                .map(RecorderResponse::from)
                .toList();
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
