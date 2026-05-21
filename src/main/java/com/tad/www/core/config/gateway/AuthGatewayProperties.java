package com.tad.www.core.config.gateway;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.auth-gateway")
public class AuthGatewayProperties {

    private String baseUrl = "http://localhost:8081/api";
}
