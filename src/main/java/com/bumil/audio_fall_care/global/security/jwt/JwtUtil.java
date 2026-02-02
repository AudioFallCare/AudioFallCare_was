package com.bumil.audio_fall_care.global.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
@Slf4j
public class JwtUtil {
    private static final String CLAIM_UID = "uid";
    private static final String CLAIM_USERNAME = "username";
    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_TYPE = "token_type";
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";

    private final SecretKey secretKey;
    private final long accessExp;
    private final long refreshExp;

    public JwtUtil(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.access-exp}") long accessExp,
            @Value("${jwt.refresh-exp}") long refreshExp
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.accessExp = accessExp;
        this.refreshExp = refreshExp;
    }

    //
    public String createAccessToken(Long userId, String username, String email) {
        long now = System.currentTimeMillis();
        Date iat = new Date(now);
        Date exp = new Date(now + accessExp);

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(String.valueOf(userId))
                .issuedAt(iat)
                .expiration(exp)
                .claim(CLAIM_UID, userId)
                .claim(CLAIM_USERNAME, username)
                .claim(CLAIM_EMAIL, email)
                .claim(CLAIM_TYPE, TYPE_ACCESS)
                .signWith(secretKey)
                .compact();
    }

    public String createRefreshToken(Long userId) {
        long now = System.currentTimeMillis();
        Date iat = new Date(now);
        Date exp = new Date(now + refreshExp);

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(String.valueOf(userId))
                .issuedAt(iat)
                .expiration(exp)
                .claim(CLAIM_UID, userId)
                .claim(CLAIM_TYPE, TYPE_REFRESH)
                .signWith(secretKey)
                .compact();
    }

    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.warn("토큰이 만료되었습니다: {}", e.getMessage());
            throw new JwtException("토큰이 만료되었습니다.", e);

        } catch (UnsupportedJwtException e) {
            log.error("지원하지 않는 토큰 형식입니다: {}", e.getMessage());
            throw new JwtException("지원하지 않는 토큰 형식입니다.", e);

        } catch (MalformedJwtException e) {
            log.error("유효하지 않은 토큰 형식입니다: {}", e.getMessage());
            throw new JwtException("유효하지 않은 토큰 형식입니다.", e);

        } catch (SecurityException | IllegalArgumentException e) {
            log.error("유효하지 않은 토큰 서명입니다: {}", e.getMessage());
            throw new JwtException("유효하지 않은 토큰 서명입니다.", e);
        }
    }

    public Long getUserId(String refreshToken) {
        Claims claims = parseToken(refreshToken);
        return claims.get(CLAIM_UID, Long.class);
    }

    public boolean isAccessToken(String token) {
        try {
            Claims claims = parseToken(token);
            return TYPE_ACCESS.equals(claims.get(CLAIM_TYPE));
        } catch (JwtException e) {
            return false;
        }
    }

    public boolean isRefreshToken(String token) {
        try {
            Claims claims = parseToken(token);
            return TYPE_REFRESH.equals(claims.get(CLAIM_TYPE));
        } catch (JwtException e) {
            return false;
        }
    }

    public long getRefreshExpMills() {
        return refreshExp;
    }
}
