package com.bumil.audio_fall_care.domain.recorder.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RecorderUserResponse {
    private Long userId;
    private String username;
}