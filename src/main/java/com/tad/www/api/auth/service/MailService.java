package com.tad.www.api.auth.service;

import java.security.SecureRandom;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.tad.www.api.auth.dto.response.MailVerificationResponse;
import com.tad.www.api.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MailService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final EmailVerificationRedisService emailVerificationRedisService;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:no-reply@tad.local}")
    private String fromEmail;

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
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject("[TAD] 이메일 인증 코드");
        message.setText("인증 코드: " + code + "\n\n해당 코드는 " + codeTtlMinutes + "분 동안 유효합니다.");
        try {
            mailSender.send(message);
        } catch (MailException e) {
            throw new RuntimeException("이메일 발송에 실패했습니다. 잠시 후 다시 시도해주세요.", e);
        }
    }

    private String createCode() {
        return String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
