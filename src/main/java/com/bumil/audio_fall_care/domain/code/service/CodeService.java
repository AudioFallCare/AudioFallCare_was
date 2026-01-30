package com.bumil.audio_fall_care.domain.code.service;

import com.bumil.audio_fall_care.domain.code.dto.response.CodeGenerateResponse;
import com.bumil.audio_fall_care.domain.code.dto.response.CodeVerifyResponse;
import com.bumil.audio_fall_care.domain.code.entity.ConnectCode;
import com.bumil.audio_fall_care.domain.code.repository.ConnectCodeRepository;
import com.bumil.audio_fall_care.domain.user.entity.User;
import com.bumil.audio_fall_care.domain.user.repository.UserRepository;
import com.bumil.audio_fall_care.global.common.BusinessException;
import com.bumil.audio_fall_care.global.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CodeService {

    private static final String CODE_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 8;

    private final ConnectCodeRepository connectCodeRepository;
    private final UserRepository userRepository;

    @Transactional
    public CodeGenerateResponse generateCode(Long userId) {
        // 기존 코드가 있으면 그대로 반환
        return connectCodeRepository.findByUserId(userId)
                .map(existing -> {
                    log.info("기존 연결 코드 반환 - userId: {}, code: {}", userId, existing.getCode());
                    return new CodeGenerateResponse(existing.getId(), existing.getCode(), existing.getExpiresAt());
                })
                .orElseGet(() -> createNewCode(userId));
    }

    @Transactional
    public CodeGenerateResponse regenerateCode(Long userId) {
        // 기존 코드 삭제
        connectCodeRepository.findByUserId(userId)
                .ifPresent(existing -> {
                    connectCodeRepository.delete(existing);
                    log.info("기존 연결 코드 삭제 - userId: {}, code: {}", userId, existing.getCode());
                });

        return createNewCode(userId);
    }

    private CodeGenerateResponse createNewCode(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String code = generateRandomCode();

        ConnectCode connectCode = ConnectCode.builder()
                .user(user)
                .code(code)
                .expiresAt(null)
                .used(false)
                .build();

        ConnectCode saved = connectCodeRepository.save(connectCode);
        log.info("연결 코드 발급 완료 - userId: {}, code: {}", userId, code);

        return new CodeGenerateResponse(saved.getId(), saved.getCode(), saved.getExpiresAt());
    }

    public CodeVerifyResponse verifyCode(String code) {
        ConnectCode connectCode = connectCodeRepository.findByCode(code)
                .orElseThrow(() -> new BusinessException(ErrorCode.CODE_NOT_FOUND));

        if (connectCode.getExpiresAt() != null && connectCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.CODE_EXPIRED);
        }

        if (Boolean.TRUE.equals(connectCode.getUsed())) {
            throw new BusinessException(ErrorCode.CODE_ALREADY_USED);
        }

        log.info("연결 코드 검증 성공 - code: {}, userId: {}", code, connectCode.getUser().getId());

        return new CodeVerifyResponse(connectCode.getUser().getId(), connectCode.getId());
    }

    private String generateRandomCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CODE_CHARACTERS.charAt(random.nextInt(CODE_CHARACTERS.length())));
        }
        return sb.toString();
    }
}
