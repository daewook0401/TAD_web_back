package com.tad.www.core.config.garage;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class GarageStorageServiceTest {

    @Test
    void buildPublicFileUrlUsesDrivePublicPathAndEncodesObjectKey() {
        GarageProperties properties = new GarageProperties();
        properties.setDrivePublicUrl("https://drive.example.com/");
        GarageStorageService storageService = new GarageStorageService(null, properties);

        String fileUrl = storageService.buildPublicFileUrl("tad", "board/posts/2/이미지 파일.png");

        assertThat(fileUrl)
            .isEqualTo("https://drive.example.com/public/tad/board/posts/2/%EC%9D%B4%EB%AF%B8%EC%A7%80%20%ED%8C%8C%EC%9D%BC.png");
    }

    @Test
    void buildPublicFileUrlUsesConfiguredDriveBase() {
        GarageProperties properties = new GarageProperties();
        properties.setDrivePublicUrl("https://drive.example.com");
        GarageStorageService storageService = new GarageStorageService(null, properties);

        assertThat(storageService.buildPublicFileUrl("tad", "folder/file.png"))
            .isEqualTo("https://drive.example.com/public/tad/folder/file.png");
    }
}
