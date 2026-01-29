package com.bumil.audio_fall_care.domain.user.service;

import com.bumil.audio_fall_care.domain.auth.dto.request.SignUpRequest;

public interface UserService {
    Long signUp(SignUpRequest dto);
}
