package com.tad.www.api.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tad.www.api.auth.dto.request.GoogleLoginRequest;
import com.tad.www.api.auth.dto.request.LoginRequest;
import com.tad.www.api.auth.dto.request.MailSendRequest;
import com.tad.www.api.auth.dto.request.MailVerifyRequest;
import com.tad.www.api.auth.dto.request.SignupRequest;
import com.tad.www.api.auth.dto.response.AuthResponse;
import com.tad.www.api.auth.dto.response.MailVerificationResponse;
import com.tad.www.api.auth.dto.token.TokenRefreshRequest;
import com.tad.www.api.auth.dto.token.TokenResponse;
import com.tad.www.api.auth.service.AuthService;
import com.tad.www.api.auth.service.JwtRefreshService;
import com.tad.www.api.auth.service.MailService;

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

    @PostMapping("/google-login")
    public ResponseEntity<AuthResponse> googleLogin(@Valid @RequestBody GoogleLoginRequest request) {
        return ResponseEntity.ok(authService.googleLogin(request.getToken()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody TokenRefreshRequest request) {
        try {
            if (request == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("INVALID_REFRESH_TOKEN");
            }

            TokenResponse response = jwtRefreshService.rotateRefreshToken(request.getRefreshToken());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<AuthResponse> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest()
            .body(AuthResponse.builder()
                .success(false)
                .message(e.getMessage())
                .build());
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<AuthResponse> handleUnsupported(UnsupportedOperationException e) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
            .body(AuthResponse.builder()
                .success(false)
                .message(e.getMessage())
                .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AuthResponse> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult()
            .getFieldErrors()
            .stream()
            .findFirst()
            .map(error -> error.getDefaultMessage())
            .orElse("요청값이 올바르지 않습니다.");

        return ResponseEntity.badRequest()
            .body(AuthResponse.builder()
                .success(false)
                .message(message)
                .build());
    }
}
