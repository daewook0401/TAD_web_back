package com.tad.www.core.config.garage;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "garage")
public class GarageProperties {

    private String endpoint;
    private String drivePublicUrl;
    private String accessKey;
    private String secretKey;
    private String bucket;
    private String region;
}
