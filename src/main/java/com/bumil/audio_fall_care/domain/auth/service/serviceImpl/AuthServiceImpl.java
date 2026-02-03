package com.bumil.audio_fall_care.domain.auth.service.serviceImpl;

import com.bumil.audio_fall_care.domain.auth.dto.request.LoginRequest;
import com.bumil.audio_fall_care.domain.auth.dto.response.LoginResult;
import com.bumil.audio_fall_care.domain.auth.dto.response.TokenPair;
import com.bumil.audio_fall_care.domain.auth.service.AuthService;
import com.bumil.audio_fall_care.domain.fcm.service.FcmTokenService;
import com.bumil.audio_fall_care.domain.user.entity.User;
import com.bumil.audio_fall_care.domain.user.service.UserService;
import com.bumil.audio_fall_care.global.security.CustomUserDetails;
import com.bumil.audio_fall_care.global.common.BusinessException;
import com.bumil.audio_fall_care.global.common.ErrorCode;
import com.bumil.audio_fall_care.global.security.jwt.JwtUtil;
import io.lettuce.core.RedisConnectionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AuthServiceImpl implements AuthService {

    // Token
    private static final String REFRESH_TOKEN_PREFIX = "RT:";

    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final FcmTokenService fcmTokenService;
    private final RedisTemplate<String, String> redisTemplate;
    private final AuthenticationManager authenticationManager;

    @Override
    public LoginResult login(LoginRequest dto) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.username(), dto.password())
            );

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Long userId = userDetails.getUserId();

            String accessToken = jwtUtil.createAccessToken(
                    userId,
                    userDetails.getUsername()
            );
            String refreshToken = jwtUtil.createRefreshToken(userId);

            // 기존 Refresh Token 삭제 (중복 로그인 가능)
            String redisKey = REFRESH_TOKEN_PREFIX + userId + ":" + dto.deviceInfo();
            redisTemplate.delete(redisKey);

            redisTemplate.opsForValue().set(
                    redisKey,
                    refreshToken,
                    jwtUtil.getRefreshExpMills(),
                    TimeUnit.MILLISECONDS
            );

            log.info("Refresh Token Redis 저장 성공: {}", userId);

            return new LoginResult(
                    userId,
                    userDetails.getUsername(),
                    new TokenPair(accessToken, refreshToken)
            );
        } catch (BadCredentialsException e) {
            log.warn("로그인 실패 - 잘못된 인증 정보: {}", dto.username());
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        } catch (UsernameNotFoundException e) {
            log.warn("로그인 실패 - 사용자를 찾을 수 없음: {}", dto.username());
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        } catch (DisabledException e) {
            log.warn("로그인 실패 - 계정 비활성화: {}", dto.username());
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        } catch (AuthenticationException e) {
            log.warn("로그인 실패 - 기타 인증 실패");
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
    }

    @Override
    public void logout(Long userId, String deviceInfo) {
        String key = REFRESH_TOKEN_PREFIX + userId + ":" + deviceInfo;

        fcmTokenService.deleteToken(userId, deviceInfo);

        try {
            Boolean deleted = redisTemplate.delete(key);

            if (deleted) {
                log.info("Refresh Token 삭제 성공 - userId={}, device={}", userId, deviceInfo);
            } else {
                log.warn("Refresh Token 삭제 대상 없음 - userId={}, device={}", userId, deviceInfo);
            }

        } catch (RedisConnectionException e) {
            log.warn("Refresh Token 삭제 실패 - userId = {}", userId, e);
        }
    }

    @Override
    public TokenPair reissueTokens(String refreshToken, String deviceInfo) {

        if (!StringUtils.hasText(refreshToken)) {
            log.warn("Refresh Token을 찾을 수 없습니다.");
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }

        if (!jwtUtil.isRefreshToken(refreshToken)) {
            log.warn("유효하지 않은 Refresh Token입니다.");
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        Long userId = jwtUtil.getUserId(refreshToken);
        String redisKey = REFRESH_TOKEN_PREFIX + userId + ":" + deviceInfo;

        try {
            String storedRefreshToken = redisTemplate.opsForValue().get(redisKey);

            if (!StringUtils.hasText(storedRefreshToken)) {
                log.warn("리프레시 토큰이 만료되었습니다.");
                throw new BusinessException(ErrorCode.REFRESH_TOKEN_EXPIRED);
            }

            if (!refreshToken.equals(storedRefreshToken)) {
                log.warn("리프레시 토큰이 일치하지 않습니다.");
                throw new BusinessException(ErrorCode.REFRESH_TOKEN_MISMATCH);
            }

            User user = userService.findById(userId);

            String newAccessToken = jwtUtil.createAccessToken(
                    userId,
                    user.getUsername()
            );
            String newRefreshToken = jwtUtil.createRefreshToken(userId);

            redisTemplate.opsForValue().set(
                    redisKey,
                    newRefreshToken,
                    jwtUtil.getRefreshExpMills(),
                    TimeUnit.MILLISECONDS
            );

            return new TokenPair(refreshToken, newAccessToken);
        } catch (RedisConnectionException e) {
            log.error("Redis 연결 실패", e);
            throw new BusinessException(ErrorCode.REDIS_CONNECTION_ERROR);
        } catch (Exception e) {
            log.error("토큰 재발급 처리 중 오류 발생", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
