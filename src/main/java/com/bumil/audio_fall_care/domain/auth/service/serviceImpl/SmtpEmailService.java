package com.bumil.audio_fall_care.domain.auth.service.serviceImpl;

import com.bumil.audio_fall_care.domain.auth.service.EmailService;
import com.bumil.audio_fall_care.global.common.BusinessException;
import com.bumil.audio_fall_care.global.common.ErrorCode;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ConditionalOnProperty(name = "email.provider", havingValue = "smtp", matchIfMissing = true)
@RequiredArgsConstructor
public class SmtpEmailService implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${email.from}")
    private String fromEmail;

    @Override
    public void send(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("SMTP 이메일 발송 성공: {}", to);
        } catch (MessagingException e) {
            log.error("이메일 발송 실패 - 이메일: {}", to, e);
            throw new BusinessException(ErrorCode.EMAIL_SEND_FAILED);
        } catch (MailException e) {
            log.error("이메일 메시지 생성 실패 - 이메일: {}", to, e);
            throw new BusinessException(ErrorCode.EMAIL_CREATE_MESSAGE_FAILED);
        }
    }
}
