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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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

        User user = User.builder()
                .username(dto.username())
                .password(passwordEncoder.encode(dto.password()))
                .email(dto.email())
                .address(new Address(dto.address(), dto.addressDetail()))
                .build();

        return  userRepository.save(user).getId();
    }
}
