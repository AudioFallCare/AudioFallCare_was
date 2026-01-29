package com.bumil.audio_fall_care.domain.auth.controller;

import com.bumil.audio_fall_care.domain.auth.dto.request.SignUpRequest;
import com.bumil.audio_fall_care.domain.user.service.UserService;
import com.bumil.audio_fall_care.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Long>> signUp(
            @Valid @RequestBody SignUpRequest dto
            ) {

        Long userId = userService.signUp(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(userId));
    }
}
