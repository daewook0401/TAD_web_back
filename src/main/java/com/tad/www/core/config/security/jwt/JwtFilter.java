package com.tad.www.core.config.security.jwt;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.tad.www.api.auth.service.RefreshTokenRedisService;
import com.tad.www.api.user.repository.UserRepository;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final RefreshTokenRedisService refreshTokenRedisService;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();

        if (path.contains("/auth/refresh")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);

            try {
                Claims claims = jwtUtil.parseJwt(token);
                if (!"access".equals(claims.get("type", String.class))) {
                    throw new IllegalArgumentException("INVALID");
                }

                UUID publicId = UUID.fromString(claims.getSubject());
                Long userId = refreshTokenRedisService.getUserId(publicId);
                if (userId == null) {
                    throw new IllegalArgumentException("INVALID");
                }

                var user = userRepository.findById(userId).orElse(null);
                if (user == null) {
                    throw new IllegalArgumentException("INVALID");
                }

                List<?> roleClaims = claims.get("roles", List.class);
                List<SimpleGrantedAuthority> authorities = roleClaims == null
                    ? List.of()
                    : roleClaims.stream()
                        .filter(String.class::isInstance)
                        .map(String.class::cast)
                        .map(SimpleGrantedAuthority::new)
                        .toList();

                UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        authorities
                    );

                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (RuntimeException e) {
                SecurityContextHolder.clearContext();

                response.setContentType(MediaType.TEXT_PLAIN_VALUE);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

                if ("EXPIRED".equals(e.getMessage())) {
                    response.getWriter().write("ACCESS_TOKEN_EXPIRED");
                } else {
                    response.getWriter().write("INVALID_TOKEN");
                }

                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
