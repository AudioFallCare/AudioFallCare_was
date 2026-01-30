package com.bumil.audio_fall_care.domain.user.service.serviceImpl;

import com.bumil.audio_fall_care.domain.auth.dto.request.SignUpRequest;
import com.bumil.audio_fall_care.domain.user.entity.Address;
import com.bumil.audio_fall_care.domain.user.entity.User;
import com.bumil.audio_fall_care.domain.user.repository.UserRepository;
import com.bumil.audio_fall_care.domain.user.service.UserService;
import com.bumil.audio_fall_care.global.common.BusinessException;
import com.bumil.audio_fall_care.global.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    @Override
    @Transactional
    public Long signUp(SignUpRequest dto) {
        if (!dto.password().equals(dto.passwordConfirm())) {
            throw new BusinessException(ErrorCode.MISMATCHED_PASSWORD);
        }

        if (userRepository.existsByUsername(dto.username())) {
            throw new BusinessException(ErrorCode.DUPLICATED_USERNAME);
        }

        if (userRepository.existsByEmail(dto.email())) {
            throw new BusinessException(ErrorCode.DUPLICATED_EMAIL);
        }

        String code;
        do {
            code = generateCode();
        } while (userRepository.existsByCode(code));


        User user = User.builder()
                .username(dto.username())
                .password(passwordEncoder.encode(dto.password()))
                .email(dto.email())
                .address(new Address(dto.address(), dto.addressDetail()))
                .code(code)
                .build();

        return  userRepository.save(user).getId();
    }


    private String generateCode() {
        StringBuilder sb = new StringBuilder(6);

        for (int i = 0; i < 6; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }

        return sb.toString();
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
