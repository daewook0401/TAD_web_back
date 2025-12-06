package com.tad.www.api.v1.auth.service;

import com.tad.www.api.v1.auth.dto.LoginRequest;
import com.tad.www.api.v1.auth.dto.LoginResponse;
import com.tad.www.api.v1.auth.dto.SignupRequest;
import com.tad.www.api.v1.auth.dto.SignupResponse;
import com.tad.www.core.domain.User;
import com.tad.www.core.repository.UserRepository;
import com.tad.www.core.config.security.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public SignupResponse signup(SignupRequest request) {
        // 이메일 중복 확인
        if (userRepository.existsByEmail(request.getEmail())) {
            return SignupResponse.builder()
                    .success(false)
                    .message("이미 존재하는 이메일입니다")
                    .build();
        }

        // 회원가입 검증
        if (request.getName() == null || request.getName().isEmpty()) {
            return SignupResponse.builder()
                    .success(false)
                    .message("사용자명을 입력해주세요")
                    .build();
        }

        if (request.getPassword() == null || request.getPassword().length() < 8) {
            return SignupResponse.builder()
                    .success(false)
                    .message("비밀번호는 최소 8자 이상이어야 합니다")
                    .build();
        }

        // 새 사용자 생성
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .tier(0)
                .rating(0)
                .wins(0)
                .losses(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);

        log.info("User signed up: {}", savedUser.getEmail());

        return SignupResponse.builder()
                .success(true)
                .message("회원가입이 완료되었습니다")
                .user(SignupResponse.UserInfo.builder()
                        .id(savedUser.getId())
                        .name(savedUser.getName())
                        .email(savedUser.getEmail())
                        .build())
                .build();
    }

    public LoginResponse login(LoginRequest request) {
        // 사용자 찾기
        var user = userRepository.findByEmail(request.getEmail());

        if (user.isEmpty()) {
            return LoginResponse.builder()
                    .success(false)
                    .message("가입되지 않은 이메일입니다")
                    .build();
        }

        User foundUser = user.get();

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), foundUser.getPassword())) {
            return LoginResponse.builder()
                    .success(false)
                    .message("비밀번호가 올바르지 않습니다")
                    .build();
        }

        // JWT 토큰 생성
        String token = jwtUtil.generateToken(foundUser.getEmail());

        log.info("User logged in: {}", foundUser.getEmail());

        return LoginResponse.builder()
                .success(true)
                .message("로그인이 완료되었습니다")
                .token(token)
                .user(LoginResponse.UserInfo.builder()
                        .id(foundUser.getId())
                        .name(foundUser.getName())
                        .email(foundUser.getEmail())
                        .build())
                .build();
    }
}
