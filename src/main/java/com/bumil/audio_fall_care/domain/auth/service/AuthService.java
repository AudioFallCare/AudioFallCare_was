package com.bumil.audio_fall_care.domain.auth.service;

import com.bumil.audio_fall_care.domain.auth.dto.request.LoginRequest;
import com.bumil.audio_fall_care.domain.auth.dto.response.LoginResult;

public interface AuthService {
    void sendVerificationCode(String toEmail);
    void verifyCode(String toEmail, String inputCode);
    LoginResult login(LoginRequest dto);
    void deleteRefreshToken(Long userId);
}
