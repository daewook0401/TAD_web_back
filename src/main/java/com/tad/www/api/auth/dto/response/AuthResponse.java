package com.tad.www.api.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {

    private boolean success;
    private String message;
    private AuthUserResponse user;
    private String token;
    private String refreshToken;
}
