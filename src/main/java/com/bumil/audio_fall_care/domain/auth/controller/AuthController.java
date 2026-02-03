package com.bumil.audio_fall_care.domain.auth.controller;

import com.bumil.audio_fall_care.domain.auth.dto.request.*;
import com.bumil.audio_fall_care.domain.auth.dto.response.LoginResponse;
import com.bumil.audio_fall_care.domain.auth.dto.response.LoginResult;
import com.bumil.audio_fall_care.domain.auth.dto.response.SignUpResponse;
import com.bumil.audio_fall_care.domain.auth.dto.response.TokenPair;
import com.bumil.audio_fall_care.domain.user.entity.User;
import com.bumil.audio_fall_care.domain.auth.service.AuthService;
import com.bumil.audio_fall_care.domain.user.service.UserService;
import com.bumil.audio_fall_care.global.common.ApiResponse;
import com.bumil.audio_fall_care.global.security.CustomUserDetails;
import com.bumil.audio_fall_care.global.security.jwt.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "인증", description = "회원가입, 이메일 인증 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";

    private final AuthService authService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Operation(summary = "회원가입", description = "새로운 보호자 계정을 생성합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 존재하는 이메일")
    })
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignUpResponse>> signUp(
            @Valid @RequestBody SignUpRequest dto
            ) {

        User user = userService.signUp(dto);
        SignUpResponse signUpResponse = new SignUpResponse(user.getId(), user.getCode());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(signUpResponse));
    }

    @Operation(
            summary = "로그인",
            description = "아이디와 비밀번호로 로그인합니다. " +
                    "Access Token은 Authorization 헤더로, " +
                    "Refresh Token은 HttpOnly Cookie로 반환됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "아이디 또는 비밀번호 불일치")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest dto,
            HttpServletResponse response
    ) {
        LoginResult loginResult = authService.login(dto);

        response.setHeader(
                "Authorization",
                "Bearer " + loginResult.tokenPair().accessToken()
        );

        addCookie(response, loginResult.tokenPair().refreshToken());

        LoginResponse loginResponse = new LoginResponse(
                loginResult.userId(),
                loginResult.username(),
                loginResult.tokenPair().accessToken()
        );

        return ResponseEntity.ok(
                ApiResponse.ok(loginResponse)
        );
    }

    @Operation(
            summary = "로그아웃",
            description = "현재 로그인된 사용자를 로그아웃합니다. " +
                    "기기(device)별 Refresh Token을 삭제하고, " +
                    "Refresh Token 쿠키를 만료 처리합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<String>> logout(
            @RequestBody @Valid LogoutRequest dto,
            HttpServletResponse response,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUserId();
        authService.logout(userId, dto.deviceInfo());

        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        log.info("로그아웃 성공 : userId = {}", userId);

        return ResponseEntity.ok(ApiResponse.ok("로그아웃에 성공했습니다."));
    }

    @Operation(
            summary = "토큰 재발급",
            description = "만료된 Access Token을 재발급합니다. " +
                    "Refresh Token은 HttpOnly Cookie에서 읽어오며, " +
                    "재발급 성공 시 Access Token은 Authorization 헤더로, " +
                    "Refresh Token은 새 Cookie로 반환됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토큰 재발급 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "유효하지 않거나 만료된 Refresh Token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<String>> refreshToken(
            @RequestBody @Valid RefreshTokenRequest dto,
            @CookieValue(name = REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken,
            HttpServletResponse response
    ) {
        TokenPair tokenPair = authService.reissueTokens(refreshToken, dto.deviceInfo());

        response.setHeader("Authorization", "Bearer " + tokenPair.accessToken());

        addCookie(response, tokenPair.refreshToken());

        log.info("토큰 재발급 성공 : userId = {}", jwtUtil.getUserId(tokenPair.refreshToken()));

        return ResponseEntity.ok(
                ApiResponse.ok("토큰 재발급에 성공했습니다.")
        );
    }




    private void addCookie(HttpServletResponse response, String token) {

        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, token)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(jwtUtil.getRefreshExpMills() / 1000)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }
}
