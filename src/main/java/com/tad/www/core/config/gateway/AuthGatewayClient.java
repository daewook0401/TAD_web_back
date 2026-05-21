package com.tad.www.core.config.gateway;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AuthGatewayClient {

    private final RestClient authGatewayRestClient;

    public AuthGatewayClient(@Qualifier("authGatewayRestClient") RestClient authGatewayRestClient) {
        this.authGatewayRestClient = authGatewayRestClient;
    }

    public TokenIntrospectionResponse introspect(String authorization) {
        try {
            TokenIntrospectionResponse response = authGatewayRestClient.post()
                .uri("/auth/introspect")
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .retrieve()
                .body(TokenIntrospectionResponse.class);

            if (response == null) {
                return TokenIntrospectionResponse.inactive("INVALID_TOKEN");
            }
            return response;
        } catch (RestClientResponseException e) {
            log.warn("Auth gateway introspection rejected request: status={}", e.getStatusCode());
            return TokenIntrospectionResponse.inactive("INVALID_TOKEN");
        } catch (RestClientException e) {
            throw new AuthGatewayUnavailableException("인증 Gateway에 연결할 수 없습니다.", e);
        }
    }
}
