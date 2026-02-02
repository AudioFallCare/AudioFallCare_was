package com.bumil.audio_fall_care.domain.auth.service;

import com.bumil.audio_fall_care.domain.auth.dto.request.LoginRequest;
import com.bumil.audio_fall_care.domain.auth.dto.response.LoginResult;
import com.bumil.audio_fall_care.domain.auth.dto.response.TokenPair;

public interface AuthService {
    void sendVerificationCode(String toEmail);
    void verifyCode(String toEmail, String inputCode);
    LoginResult login(LoginRequest dto);
    void logout(Long userId, String deviceInfo);
    TokenPair reissueTokens(String refreshToken, String deviceInfo);
}
