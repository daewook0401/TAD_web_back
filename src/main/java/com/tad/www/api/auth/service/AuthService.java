package com.tad.www.api.auth.service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tad.www.api.auth.dto.request.LoginRequest;
import com.tad.www.api.auth.dto.request.SignupRequest;
import com.tad.www.api.auth.dto.response.AuthResponse;
import com.tad.www.api.auth.dto.response.AuthUserResponse;
import com.tad.www.api.auth.entity.UserRole;
import com.tad.www.api.auth.repository.RoleRepository;
import com.tad.www.api.auth.repository.UserRoleRepository;
import com.tad.www.api.user.entity.User;
import com.tad.www.api.user.repository.UserRepository;
import com.tad.www.core.config.security.jwt.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRedisService refreshTokenRedisService;
    private final EmailVerificationRedisService emailVerificationRedisService;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        if (!emailVerificationRedisService.isVerified(normalizedEmail)) {
            throw new IllegalArgumentException("이메일 인증을 완료해주세요.");
        }

        User user = User.builder()
            .email(normalizedEmail)
            .nickname(request.getName().trim())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .emailVerified(true)
            .status("ACTIVE")
            .build();

        User savedUser = userRepository.save(user);
        Long roleId = roleRepository.findByRoleName("ROLE_USER")
            .orElseThrow(() -> new IllegalStateException("기본 권한 ROLE_USER가 존재하지 않습니다."))
            .getId();

        userRoleRepository.save(UserRole.builder()
            .userId(savedUser.getId())
            .roleId(roleId)
            .build());

        List<String> roles = userRoleRepository.findRoleNamesByUserId(savedUser.getId());
        emailVerificationRedisService.clear(normalizedEmail);

        return AuthResponse.builder()
            .success(true)
            .message("회원가입이 완료되었습니다.")
            .user(AuthUserResponse.from(savedUser, roles))
            .build();
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());
        User user = userRepository.findByEmail(normalizedEmail)
            .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));

        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new IllegalArgumentException("비활성화된 계정입니다.");
        }

        user.setLastLoginAt(OffsetDateTime.now());
        List<String> roles = userRoleRepository.findRoleNamesByUserId(user.getId());

        UUID publicId = UUID.randomUUID();
        String accessToken = jwtUtil.getAccessToken(publicId.toString(), roles);
        String refreshToken = jwtUtil.getRefreshToken(publicId.toString(), roles);

        refreshTokenRedisService.save(
            publicId,
            user.getId(),
            refreshToken,
            Duration.ofMinutes(jwtUtil.getRefreshTokenMinutes())
        );

        return AuthResponse.builder()
            .success(true)
            .message("로그인에 성공했습니다.")
            .user(AuthUserResponse.from(user, roles))
            .token(accessToken)
            .refreshToken(refreshToken)
            .build();
    }

    public AuthResponse googleLogin(String credentialToken) {
        if (credentialToken == null || credentialToken.isBlank()) {
            throw new IllegalArgumentException("Google credential token is required.");
        }

        throw new UnsupportedOperationException("Google 로그인 검증 로직은 아직 구현되지 않았습니다.");
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
