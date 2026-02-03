package com.bumil.audio_fall_care.domain.code.service;

import com.bumil.audio_fall_care.domain.code.dto.response.CodeGenerateResponse;
import com.bumil.audio_fall_care.domain.code.dto.response.CodeVerifyResponse;
import com.bumil.audio_fall_care.domain.user.entity.User;
import com.bumil.audio_fall_care.domain.user.repository.UserRepository;
import com.bumil.audio_fall_care.global.common.BusinessException;
import com.bumil.audio_fall_care.global.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CodeService {

    private static final String CODE_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 6;

    private final UserRepository userRepository;

    public CodeGenerateResponse generateCode(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        log.info("연결 코드 반환 - userId: {}, code: {}", userId, user.getCode());
        return new CodeGenerateResponse(user.getCode());
    }

    @Transactional
    public CodeGenerateResponse regenerateCode(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String newCode;
        do {
            newCode = generateRandomCode();
        } while (userRepository.existsByCode(newCode));

        user.updateCode(newCode);

        log.info("연결 코드 재발급 완료 - userId: {}, code: {}", userId, newCode);
        return new CodeGenerateResponse(newCode);
    }

    public CodeVerifyResponse verifyCode(String code) {
        User user = userRepository.findByCode(code)
                .orElseThrow(() -> new BusinessException(ErrorCode.CODE_NOT_FOUND));

        log.info("연결 코드 검증 성공 - code: {}, userId: {}", code, user.getId());
        return new CodeVerifyResponse(user.getId());
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
