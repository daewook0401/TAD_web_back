package com.tad.www.api.user.service;

import org.springframework.stereotype.Service;

import com.tad.www.api.user.entity.User;
import com.tad.www.api.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserService {
    
    private final UserRepository userRepository;

    public String signup() {
        
        return "회원가입 성공";
    }

    // 추후 익셉션 추가
    public User findByEmail(String email){
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        return user;
    }
}
