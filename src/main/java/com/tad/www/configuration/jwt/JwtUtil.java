package com.tad.www.configuration.jwt;

import java.util.Base64;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secretKey;
    private SecretKey key;

    @PostConstruct
    public void init(){
        byte[] keyArr = Base64.getDecoder().decode(secretKey);
        //this.key = Keys.hmacShaKeyFor(keyArr);
    }
    
}
