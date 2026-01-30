package com.bumil.audio_fall_care.global.security.jwt;

import com.bumil.audio_fall_care.global.security.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    @Value("${security.whitelist}")
    private String[] whiteList;
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private final JwtUtil jwtUtil;


    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {

            // preflight 요청 바로 통과
            if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                filterChain.doFilter(request, response);
                return;
            }

            String uri = request.getRequestURI();

            // whitelist 통과
            if (isWhiteListed(uri)) {
                filterChain.doFilter(request, response);
                return;
            }

            // 이미 인증된 경우 통과
            Authentication existing = SecurityContextHolder.getContext().getAuthentication();
            if (existing != null && !(existing instanceof AnonymousAuthenticationToken)) {
                filterChain.doFilter(request, response);
                return;
            }

            String accessToken = extractAccessToken(request);

            // Access Token 없으면 통과 (API 알아서 401 처리)
            if (!StringUtils.hasText(accessToken)) {
                filterChain.doFilter(request, response);
                return;
            }

            Claims claims;
            try {
                claims = jwtUtil.parseToken(accessToken);
            } catch (ExpiredJwtException e) {
                log.debug("Access Token 만료: {}", uri);
                request.setAttribute("token.error", "expired_token");
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            } catch (JwtException e) {
                log.debug("Access Token 위조/파싱 실패 - uri={}, message={}",uri, e.getMessage());
                request.setAttribute("token.error", "invalid_token");
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            if (!jwtUtil.isAccessToken(accessToken)) {
                log.debug("Access Token이 아닙니다. {}", uri);
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            setAuthenticationFromClaims(claims);
            log.debug("유효한 Access Token입니다. 요청한 URL: {}", uri);
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.warn("JWT 필터 처리 중 예외 발생: {}", e.getMessage());
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
        }
    }




    private boolean isWhiteListed(String uri) {
        return Arrays.stream(whiteList)
                .anyMatch(
                        pattern -> PATH_MATCHER.match(pattern, uri)
                );
    }

    private String extractAccessToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (!StringUtils.hasText(bearerToken) || !bearerToken.startsWith("Bearer ")) {
            return null;
        }

        return bearerToken.substring(7);
    }

    private void setAuthenticationFromClaims(Claims claims) {
        try {
            Number uidNum = claims.get("uid", Number.class);
            Long userId = uidNum != null ? uidNum.longValue() : null;
            String username = claims.get("username", String.class);
            String email = claims.get("email", String.class);

            CustomUserDetails userDetails = new CustomUserDetails(userId, username, email);

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("사용자 {}의 인증 설정을 성공했습니다. - userId: {}", username, userId);
        } catch (Exception e) {
            log.error("인증을 설정하는데 실패했습니다. {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }
    }
}
