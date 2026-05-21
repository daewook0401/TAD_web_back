package com.tad.www.core.config.security.jwt;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tad.www.api.user.entity.User;
import com.tad.www.core.config.gateway.AuthGatewayClient;
import com.tad.www.core.config.gateway.AuthGatewayUnavailableException;
import com.tad.www.core.config.gateway.TokenIntrospectionResponse;
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

    private final AuthGatewayClient authGatewayClient;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getServletPath();
        String method = request.getMethod();

        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorization != null && authorization.startsWith("Bearer ")) {
            try {
                TokenIntrospectionResponse introspection = authGatewayClient.introspect(authorization);
                if (!introspection.isActive()) {
                    throw new IllegalArgumentException(introspection.getMessage());
                }

                User user = User.builder()
                    .id(introspection.getUserId())
                    .email(introspection.getEmail())
                    .nickname(introspection.getNickname())
                    .pictureUrl(introspection.getProfileImageUrl())
                    .status(introspection.getStatus())
                    .build();

                List<String> roles = introspection.getRoles();
                List<SimpleGrantedAuthority> authorities = roles == null
                    ? List.of()
                    : roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList();

                UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        authorities
                    );

                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (AuthGatewayUnavailableException e) {
                SecurityContextHolder.clearContext();

                if (isPublicPath(path, method)) {
                    filterChain.doFilter(request, response);
                    return;
                }

                writeErrorResponse(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE, "AUTH_GATEWAY_UNAVAILABLE");
                return;
            } catch (RuntimeException e) {
                SecurityContextHolder.clearContext();

                if (isPublicPath(path, method)) {
                    filterChain.doFilter(request, response);
                    return;
                }

                writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "ACCESS_TOKEN_EXPIRED".equals(e.getMessage())
                    ? "ACCESS_TOKEN_EXPIRED"
                    : "INVALID_TOKEN");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicPath(String path, String method) {
        return (("GET".equalsIgnoreCase(method)) && (path.startsWith("/board/") || "/board".equals(path)))
            || "/health".equals(path);
    }

    private void writeErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(Map.of(
            "success", false,
            "message", message
        )));
    }
}
