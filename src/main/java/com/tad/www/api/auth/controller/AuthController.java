package com.tad.www.api.auth.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.tad.www.api.auth.dto.signup.SignupRequest;
import com.tad.www.api.auth.dto.signup.SignupResponse;
import com.tad.www.api.auth.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    // @PostMapping("/signup")
    // public ResponseEntity<String> signup(@RequestBody SignupRequest request) {
    //     log.info("Signup request: {}", request.getEmail());
    //     SignupResponse response = authService.signup(request);
    //     return ResponseEntity.ok("response");
    // }

    @PostMapping("/mail")
    public ResponseEntity<String> postMethodName(@RequestBody String entity) {
        
        

        return ResponseEntity.ok("메일 전송 성공");
    }
    

    // @PostMapping("/login")
    // public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
    //     log.info("Login request: {}", request.getEmail());
    //     LoginResponse response = authService.login(request);
    //     return response.isSuccess() ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
    // }
}
