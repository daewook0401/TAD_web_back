package com.tad.www.core.config.analysis;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(AnalysisProperties.class)
public class AnalysisConfig {

    @Bean
    public RestClient analysisRestClient(AnalysisProperties properties) {
        return RestClient.builder()
            .baseUrl(properties.getServiceUrl())
            .build();
    }
}
