package com.tad.www.api.user.service;

import org.springframework.stereotype.Service;

import com.tad.www.api.user.entity.User;
import com.tad.www.api.user.exception.UserNotFoundException;
import com.tad.www.api.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    public String signup() {

        return "?뚯썝媛???깃났";
    }

    // 異뷀썑 ?듭뀎??異붽?
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));
    }
}
