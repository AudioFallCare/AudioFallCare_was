package com.bumil.audio_fall_care.domain.auth.service.serviceImpl;

import com.bumil.audio_fall_care.domain.auth.service.AuthService;
import com.bumil.audio_fall_care.global.common.BusinessException;
import com.bumil.audio_fall_care.global.common.ErrorCode;
import io.lettuce.core.RedisConnectionException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AuthServiceImpl implements AuthService {

    // Email
    @Value("${spring.mail.username}")
    private String fromEmail;
    private static final String EMAIL_VERIFICATION_PREFIX = "EMAIL:";
    private static final int EXPIRATION_MINUTES = 3;

    private final JavaMailSender mailSender;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void sendVerificationCode(String toEmail) {
        try {
            String verificationCode = generateVerificationCode();

            String key = EMAIL_VERIFICATION_PREFIX + toEmail;
            redisTemplate.opsForValue().set(
                    key,
                    verificationCode,
                    Duration.ofMinutes(EXPIRATION_MINUTES)
            );

            sendEmailCode(toEmail, verificationCode);
            log.info("인증번호 발송 완료: {}", toEmail);
        } catch (RedisConnectionException e) {
            log.error("Redis 연결 실패", e);
            throw new BusinessException(ErrorCode.REDIS_CONNECTION_ERROR);
        } catch (MailException e) {
            log.error("이메일 발송 실패 - 이메일: {}", toEmail, e);
            throw new BusinessException(ErrorCode.EMAIL_SEND_FAILED);
        } catch (Exception e) {
            log.error("인증번호 발송 처리 중 오류 발생", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void verifyCode(String toEmail, String inputCode) {
        try {
            String key = EMAIL_VERIFICATION_PREFIX + toEmail;
            String storedCode = redisTemplate.opsForValue().get(key);

            if (storedCode == null) {
                log.warn("인증번호가 만료되었습니다. - 이메일: {}", toEmail);
                throw new BusinessException(ErrorCode.VERIFICATION_CODE_EXPIRED);
            }

            if (!storedCode.equals(inputCode.trim())) {
                log.warn("인증번호가 일치하지 않습니다. - 이메일: {}", toEmail);
                throw new BusinessException(ErrorCode.INVALID_VERIFICATION_CODE);
            }

            redisTemplate.delete(key);
        } catch (RedisConnectionException e) {
            log.error("Redis 연결 실패", e);
            throw new BusinessException(ErrorCode.REDIS_CONNECTION_ERROR);
        } catch (Exception e) {
            log.error("인증번호 검증 도중 오류 발생", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private String generateVerificationCode() {
        SecureRandom random = new SecureRandom();
        return String.format("%06d", random.nextInt(1_000_000));
    }

    private void sendEmailCode(String toEmail, String verificationCode) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("낙상지킴이 회원가입 이메일 인증");

            String htmlContent = createEmailTemplate(verificationCode);

            helper.setText(htmlContent, true);
            mailSender.send(message);

            log.info("이메일 발송 성공: {}", toEmail);
        } catch (MessagingException e) {
            log.error("이메일 발송 실패 - 이메일: {}", toEmail, e);
            throw new BusinessException(ErrorCode.EMAIL_SEND_FAILED);
        } catch (MailException e) {
            log.error("이메일 메시지 생성 실패 - 이메일: {}", toEmail, e);
            throw new BusinessException(ErrorCode.EMAIL_CREATE_MESSAGE_FAILED);
        } catch (Exception e) {
            log.error("이메일 처리 중 오류 발생", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private String createEmailTemplate(String verificationCode) {
        return String.format(
                "<html><body style='font-family: Arial, sans-serif;'>" +
                        "<div style='max-width: 600px; margin: 0 auto; padding: 20px;'>" +
                        "<h2 style='color: #333; text-align: center;'>회원가입 이메일 인증</h2>" +
                        "<div style='background-color: #f8f9fa; padding: 20px; border-radius: 8px; text-align: center;'>" +
                        "<p style='font-size: 16px; margin-bottom: 20px;'>아래 인증번호를 입력해주세요:</p>" +
                        "<div style='font-size: 32px; font-weight: bold; color: #007bff; letter-spacing: 5px; margin: 20px 0;'>%s</div>" +
                        "<p style='color: #dc3545; font-weight: bold;'>3분 이내에 입력해주세요!</p>" +
                        "</div>" +
                        "<p style='font-size: 14px; color: #666; text-align: center; margin-top: 20px;'>" +
                        "본인이 요청하지 않았다면 이 이메일을 무시해주세요." +
                        "</p>" +
                        "</div>" +
                        "</body></html>",
                verificationCode
        );
    }
}
