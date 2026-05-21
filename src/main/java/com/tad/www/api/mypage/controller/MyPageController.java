package com.tad.www.api.mypage.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tad.www.api.auth.dto.response.MyPageSummaryResponse;
import com.tad.www.api.auth.service.MyPageService;
import com.tad.www.api.user.entity.User;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mypage")
public class MyPageController {

    private final MyPageService myPageService;

    @GetMapping("/summary")
    public ResponseEntity<MyPageSummaryResponse> getMyPageSummary(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(myPageService.getSummary(currentUser));
    }
}
