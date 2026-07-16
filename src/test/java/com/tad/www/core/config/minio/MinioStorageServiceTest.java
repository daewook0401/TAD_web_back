package com.tad.www.core.config.minio;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MinioStorageServiceTest {

    @Test
    void buildPublicFileUrlUsesDrivePublicPathAndEncodesObjectKey() {
        MinioProperties properties = new MinioProperties();
        properties.setDrivePublicUrl("https://drive.example.com/");
        MinioStorageService storageService = new MinioStorageService(null, properties);

        String fileUrl = storageService.buildPublicFileUrl("tad", "board/posts/2/이미지 파일.png");

        assertThat(fileUrl)
            .isEqualTo("https://drive.example.com/public/tad/board/posts/2/%EC%9D%B4%EB%AF%B8%EC%A7%80%20%ED%8C%8C%EC%9D%BC.png");
    }

    @Test
    void buildPublicFileUrlUsesLegacyStorageUrlUntilDrivePublicUrlIsConfigured() {
        MinioProperties properties = new MinioProperties();
        properties.setPublicUrl("https://storage.example.com");
        MinioStorageService storageService = new MinioStorageService(null, properties);

        assertThat(storageService.buildPublicFileUrl("tad", "folder/file.png"))
            .isEqualTo("https://storage.example.com/tad/folder/file.png");
    }
}
