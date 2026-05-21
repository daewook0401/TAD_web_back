package com.tad.www.core.config.gateway;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenIntrospectionResponse {

    private boolean active;
    private String message;
    private Long userId;
    private String email;
    private String nickname;
    private String profileImageUrl;
    private String status;
    private List<String> roles;

    public static TokenIntrospectionResponse inactive(String message) {
        TokenIntrospectionResponse response = new TokenIntrospectionResponse();
        response.setActive(false);
        response.setMessage(message);
        return response;
    }
}
