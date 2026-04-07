package com.tad.www.api.auth.service;

import java.time.Duration;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.tad.www.api.auth.dto.mail.MailDto;
import com.tad.www.api.auth.entity.Mail;
import com.tad.www.api.auth.repository.MailRepository;
import com.tad.www.api.user.service.UserService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MailService {

    private final EmailVerificationRedisService emailVerificationRedisService;
    private final MailRepository mailRepository;
    private final UserService userService;

    private static final String PREFIX = "email:verification:";
    private static final long CODE_TTL_MINUTES = 3;

    @SuppressWarnings("null")
    @Transactional
    public ResponseEntity<MailDto> sendMail(String email){

        // email 유저 조회
        userService.findByEmail(email);

        // UUID 생성 + 인증코드 생성
        UUID requestId = UUID.randomUUID();
        String codeKey = PREFIX + requestId;

        String code = createCode();

        // 인증코드 DB 저장
        mailRepository.save(Mail.create(requestId, email, "verification"));

        // 인증코드 저장 (Redis)
        emailVerificationRedisService.save(codeKey, code, Duration.ofMinutes(CODE_TTL_MINUTES));

        // 이메일 발송
        send(email, code);

        return ResponseEntity.ok(MailDto.builder()
                .requestId(requestId)
                .build());
    }

    private void send(String email, String code){

    }

    private String createCode() {
        return String.valueOf((int)((Math.random() * 900000) + 100000));
    }
}
