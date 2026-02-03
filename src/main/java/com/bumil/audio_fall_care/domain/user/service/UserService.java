package com.bumil.audio_fall_care.domain.user.service;

import com.bumil.audio_fall_care.domain.auth.dto.request.SignUpRequest;
import com.bumil.audio_fall_care.domain.user.entity.User;

public interface UserService {
    User findByUsername(String username);
    User signUp(SignUpRequest dto);
    User findById(Long id);
}
