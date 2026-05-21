package com.tad.www.api.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tad.www.api.auth.dto.request.ChangePasswordRequest;
import com.tad.www.api.auth.dto.request.UpdateProfileRequest;
import com.tad.www.api.auth.dto.response.ProfileResponse;
import com.tad.www.api.auth.dto.response.SuccessResponse;
import com.tad.www.api.auth.service.AuthService;
import com.tad.www.api.user.entity.User;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@Profile("!legacy-auth")
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AccountController {

    private final AuthService authService;

    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getMyProfile(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(authService.getMyProfile(currentUser));
    }

    @PutMapping("/me")
    public ResponseEntity<ProfileResponse> updateMyProfile(
        @AuthenticationPrincipal User currentUser,
        @Valid @RequestBody UpdateProfileRequest request
    ) {
        return ResponseEntity.ok(authService.updateMyProfile(currentUser, request));
    }

    @PutMapping("/me/password")
    public ResponseEntity<SuccessResponse> changePassword(
        @AuthenticationPrincipal User currentUser,
        @Valid @RequestBody ChangePasswordRequest request
    ) {
        return ResponseEntity.ok(authService.changePassword(currentUser, request));
    }
}
