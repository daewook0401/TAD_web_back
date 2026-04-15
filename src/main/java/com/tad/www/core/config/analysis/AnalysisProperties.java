package com.tad.www.core.config.analysis;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "analysis")
public class AnalysisProperties {

    private String serviceUrl;
}
