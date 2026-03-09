package com.tad.www.api.auth.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.tad.www.api.auth.dto.mail.MailDto;
import com.tad.www.api.user.service.UserService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class MailService {

    private final UserService userService;

    public ResponseEntity<MailDto> sendMail(String email){
        // email 유저 조회
        userService.findByEmail(email);

        // UUID 생성 + 인증코드 생성
        UUID requestId = UUID.randomUUID();

        // 인증코드 저장 (Redis)

        // 인증코드 DB 저장
        
        // 이메일 발송

        // return ResponseEntity.ok(MailDto.builder()
        //         .requestId(requestId)
        //         .build());
        return null;
    }
}
