package com.tad.www.api.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.tad.www.api.auth.entity.LoginHistory;
import com.tad.www.api.auth.repository.LoginHistoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoginHistoryService {

    private final LoginHistoryRepository loginHistoryRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(Long userId, String loginType, String loginResult, String ipAddress, String userAgent) {
        loginHistoryRepository.save(LoginHistory.builder()
            .userId(userId)
            .ipAddress(ipAddress)
            .userAgent(userAgent)
            .loginType(loginType)
            .loginResult(loginResult)
            .build());
    }
}
