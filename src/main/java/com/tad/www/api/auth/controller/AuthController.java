package com.tad.www.api.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tad.www.api.auth.dto.mail.MailDto;
import com.tad.www.api.auth.service.MailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final MailService mailService;

    @PostMapping("/mail")
    public ResponseEntity<MailDto> postMethodName(@RequestBody String email) {
        return mailService.sendMail(email);
    }
    
}
