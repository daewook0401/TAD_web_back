package com.tad.www.api.auth.service;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailVerificationRedisService {

    private final StringRedisTemplate stringRedisTemplate;

    public void save(String key, String value, Duration ttl){
        stringRedisTemplate.opsForValue().set(key, value, ttl);
    }

    public String get(String key){
        return stringRedisTemplate.opsForValue().get(key);
    }

    public void delete(String key){
        stringRedisTemplate.delete(key);
    }
}
