package com.bumil.audio_fall_care.domain.auth.service;

public interface EmailService {
    void send(String to, String subject, String htmlContent);
}
