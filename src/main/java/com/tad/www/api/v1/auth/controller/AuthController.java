package com.tad.www.api.v1.auth.controller;

import com.tad.www.api.v1.auth.dto.LoginRequest;
import com.tad.www.api.v1.auth.dto.LoginResponse;
import com.tad.www.api.v1.auth.dto.SignupRequest;
import com.tad.www.api.v1.auth.dto.SignupResponse;
import com.tad.www.api.v1.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@RequestBody SignupRequest request) {
        log.info("Signup request: {}", request.getEmail());
        SignupResponse response = authService.signup(request);
        return response.isSuccess() ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        log.info("Login request: {}", request.getEmail());
        LoginResponse response = authService.login(request);
        return response.isSuccess() ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
    }
}
