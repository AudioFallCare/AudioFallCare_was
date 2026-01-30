package com.bumil.audio_fall_care.domain.auth.service;

public interface AuthService {
    void sendVerificationCode(String toEmail);
    void verifyCode(String toEmail, String inputCode);
}
