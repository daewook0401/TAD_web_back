package com.tad.www.api.auth.service;

import com.tad.www.api.auth.dto.signup.SignupRequest;

public interface AuthService {
    void Signup(SignupRequest request);
}
