package com.tad.www.api.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tad.www.api.auth.dto.request.ChangePasswordRequest;
import com.tad.www.api.auth.dto.request.GoogleLoginRequest;
import com.tad.www.api.auth.dto.request.LoginRequest;
import com.tad.www.api.auth.dto.request.MailSendRequest;
import com.tad.www.api.auth.dto.request.MailVerifyRequest;
import com.tad.www.api.auth.dto.request.SignupRequest;
import com.tad.www.api.auth.dto.request.UpdateProfileRequest;
import com.tad.www.api.auth.dto.response.AuthResponse;
import com.tad.www.api.auth.dto.response.MailVerificationResponse;
import com.tad.www.api.auth.dto.response.ProfileResponse;
import com.tad.www.api.auth.dto.response.SuccessResponse;
import com.tad.www.api.auth.dto.token.TokenRefreshRequest;
import com.tad.www.api.auth.dto.token.TokenResponse;
import com.tad.www.api.auth.service.AuthService;
import com.tad.www.api.auth.service.JwtRefreshService;
import com.tad.www.api.auth.service.MailService;
import com.tad.www.api.user.entity.User;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final MailService mailService;
    private final JwtRefreshService jwtRefreshService;
    private final AuthService authService;

    @PostMapping("/mail")
    public ResponseEntity<MailVerificationResponse> sendMail(@Valid @RequestBody MailSendRequest request) {
        return ResponseEntity.ok(mailService.sendMail(request.getEmail()));
    }

    @PostMapping("/mail/verify")
    public ResponseEntity<MailVerificationResponse> verifyMail(@Valid @RequestBody MailVerifyRequest request) {
        return ResponseEntity.ok(mailService.verify(request.getEmail(), request.getCode()));
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.signup(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getMyProfile(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(authService.getMyProfile(currentUser));
    }

    @PutMapping("/me")
    public ResponseEntity<ProfileResponse> updateMyProfile(
        @AuthenticationPrincipal User currentUser,
        @Valid @RequestBody UpdateProfileRequest request
    ) {
        return ResponseEntity.ok(authService.updateMyProfile(currentUser, request));
    }

    @PutMapping("/me/password")
    public ResponseEntity<SuccessResponse> changePassword(
        @AuthenticationPrincipal User currentUser,
        @Valid @RequestBody ChangePasswordRequest request
    ) {
        return ResponseEntity.ok(authService.changePassword(currentUser, request));
    }

    @PostMapping("/google-login")
    public ResponseEntity<AuthResponse> googleLogin(@Valid @RequestBody GoogleLoginRequest request) {
        return ResponseEntity.ok(authService.googleLogin(request.getToken()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestBody TokenRefreshRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("INVALID_REFRESH_TOKEN");
        }

        TokenResponse response = jwtRefreshService.rotateRefreshToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }
}
