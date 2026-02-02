package com.bumil.audio_fall_care.domain.auth.service.serviceImpl;

import com.bumil.audio_fall_care.domain.auth.service.EmailService;
import com.bumil.audio_fall_care.global.common.BusinessException;
import com.bumil.audio_fall_care.global.common.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@ConditionalOnProperty(name = "email.provider", havingValue = "brevo")
public class BrevoEmailService implements EmailService {

    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";

    private final String apiKey;
    private final String fromEmail;
    private final RestTemplate restTemplate;

    public BrevoEmailService(
            @Value("${brevo.api-key}") String apiKey,
            @Value("${email.from}") String fromEmail) {
        this.apiKey = apiKey;
        this.fromEmail = fromEmail;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public void send(String to, String subject, String htmlContent) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", apiKey);

            Map<String, Object> body = Map.of(
                    "sender", Map.of("email", fromEmail),
                    "to", List.of(Map.of("email", to)),
                    "subject", subject,
                    "htmlContent", htmlContent
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(BREVO_API_URL, request, String.class);

            log.info("Brevo 이메일 발송 성공: {}", to);
        } catch (Exception e) {
            log.error("Brevo 이메일 발송 실패 - 이메일: {}", to, e);
            throw new BusinessException(ErrorCode.EMAIL_SEND_FAILED);
        }
    }
}
