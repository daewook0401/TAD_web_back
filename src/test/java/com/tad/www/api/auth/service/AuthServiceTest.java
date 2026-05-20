package com.tad.www.api.auth.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.tad.www.api.auth.dto.request.LoginRequest;
import com.tad.www.api.auth.repository.RoleRepository;
import com.tad.www.api.auth.repository.UserRoleRepository;
import com.tad.www.api.user.entity.User;
import com.tad.www.api.user.repository.UserRepository;
import com.tad.www.core.config.security.jwt.JwtUtil;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RefreshTokenRedisService refreshTokenRedisService;

    @Mock
    private EmailVerificationRedisService emailVerificationRedisService;

    @Mock
    private LoginHistoryService loginHistoryService;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @InjectMocks
    private AuthService authService;

    @Test
    void loginRecordsFailedAttemptForExistingUser() {
        LoginRequest request = new LoginRequest();
        request.setEmail("USER@example.com");
        request.setPassword("wrong-password");

        User user = User.builder()
            .id(1L)
            .email("user@example.com")
            .nickname("user")
            .passwordHash("encoded-password")
            .status("ACTIVE")
            .build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("이메일 또는 비밀번호가 올바르지 않습니다.");

        verify(loginHistoryService).record(eq(1L), eq("NORMAL"), eq("FAILURE"), any(), any());
    }

    @Test
    void loginDoesNotRecordUnknownUserAttempt() {
        LoginRequest request = new LoginRequest();
        request.setEmail("missing@example.com");
        request.setPassword("wrong-password");

        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("이메일 또는 비밀번호가 올바르지 않습니다.");

        verify(loginHistoryService, never()).record(any(), any(), any(), any(), any());
    }
}
