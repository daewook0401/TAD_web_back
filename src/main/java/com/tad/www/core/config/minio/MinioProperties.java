package com.tad.www.core.config.minio;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {

    private String endpoint;
    private String publicUrl;
    private String drivePublicUrl;
    private String accessKey;
    private String secretKey;
    private String bucket;
}
