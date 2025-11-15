package com.tad.www.configuration.jwt;

import java.util.Base64;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
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
        this.key = Keys.hmacShaKeyFor(keyArr);
    }

    public String getAccessToken(String memberId){
        return Jwts.builder()
                    .subject(memberId)
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + (3600000L * 24)))
                    .signWith(key)
                    .compact();
    }

    public String getRefreshToken(String memberId){
        return Jwts.builder()
                    .subject(memberId)
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + (3600000L * 24 * 3)))
                    .signWith(key)
                    .compact();
    }
    public String getRefreshToken(String memberId, int day){
        return Jwts.builder()
                    .subject(memberId)
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + (3600000L * 24 * day)))
                    .signWith(key)
                    .compact();
    }
}
