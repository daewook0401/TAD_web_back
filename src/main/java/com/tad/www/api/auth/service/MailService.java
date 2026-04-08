package com.tad.www.api.auth.service;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.tad.www.api.auth.dto.response.MailVerificationResponse;
import com.tad.www.api.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MailService {

    private final EmailVerificationRedisService emailVerificationRedisService;
    private final UserRepository userRepository;

    @Value("${app.email-verification.code-minutes:3}")
    private long codeTtlMinutes;

    @Value("${app.email-verification.verified-minutes:30}")
    private long verifiedTtlMinutes;

    public MailVerificationResponse sendMail(String email) {
        String normalizedEmail = normalizeEmail(email);
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        String code = createCode();
        emailVerificationRedisService.clear(normalizedEmail);
        emailVerificationRedisService.saveCode(normalizedEmail, code, Duration.ofMinutes(codeTtlMinutes));
        send(normalizedEmail, code);

        return MailVerificationResponse.builder()
            .success(true)
            .message("인증 코드가 발송되었습니다.")
            .email(normalizedEmail)
            .verified(false)
            .build();
    }

    public MailVerificationResponse verify(String email, String code) {
        String normalizedEmail = normalizeEmail(email);
        String savedCode = emailVerificationRedisService.getCode(normalizedEmail);
        if (savedCode == null) {
            throw new IllegalArgumentException("인증 코드가 만료되었거나 존재하지 않습니다.");
        }

        if (!savedCode.equals(code.trim())) {
            throw new IllegalArgumentException("인증 코드가 올바르지 않습니다.");
        }

        emailVerificationRedisService.clearCode(normalizedEmail);
        emailVerificationRedisService.markVerified(normalizedEmail, Duration.ofMinutes(verifiedTtlMinutes));

        return MailVerificationResponse.builder()
            .success(true)
            .message("이메일 인증이 완료되었습니다.")
            .email(normalizedEmail)
            .verified(true)
            .build();
    }

    private void send(String email, String code) {
    }

    private String createCode() {
        return String.valueOf((int) ((Math.random() * 900000) + 100000));
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
