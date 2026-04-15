package com.tad.www.core.config.minio;

import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MinioStorageService {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    public StoredObject store(MultipartFile file, String objectPrefix) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 필요합니다.");
        }

        ensureBucketExists();

        String originalFileName = sanitizeFileName(file.getOriginalFilename());
        String extension = extractExtension(originalFileName);
        String storedName = UUID.randomUUID() + extension;
        String normalizedPrefix = normalizePrefix(objectPrefix);
        String objectKey = normalizedPrefix.isEmpty() ? storedName : normalizedPrefix + "/" + storedName;
        String contentType = normalizeContentType(file.getContentType(), originalFileName);

        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .object(objectKey)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(contentType)
                    .build()
            );
        } catch (Exception e) {
            throw new IllegalStateException("MinIO 파일 업로드에 실패했습니다.", e);
        }

        return new StoredObject(
            minioProperties.getBucket(),
            objectKey,
            buildFileUrl(objectKey),
            originalFileName,
            storedName,
            contentType,
            file.getSize()
        );
    }

    public String normalizeContentType(String contentType, String fileName) {
        if (contentType != null && !contentType.isBlank()) {
            return contentType.toLowerCase(Locale.ROOT);
        }

        String extension = extractExtension(fileName).toLowerCase(Locale.ROOT);
        return switch (extension) {
            case ".png" -> "image/png";
            case ".jpg", ".jpeg" -> "image/jpeg";
            case ".gif" -> "image/gif";
            case ".webp" -> "image/webp";
            case ".pdf" -> "application/pdf";
            case ".txt" -> "text/plain";
            default -> "application/octet-stream";
        };
    }

    private void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .build()
            );

            if (!exists) {
                minioClient.makeBucket(
                    MakeBucketArgs.builder()
                        .bucket(minioProperties.getBucket())
                        .build()
                );
            }
        } catch (Exception e) {
            throw new IllegalStateException("MinIO bucket 확인에 실패했습니다.", e);
        }
    }

    private String buildFileUrl(String objectKey) {
        String baseUrl = minioProperties.getPublicUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = minioProperties.getEndpoint();
        }
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl + "/" + minioProperties.getBucket() + "/" + objectKey;
    }

    private String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "file";
        }

        String normalized = Paths.get(fileName).getFileName().toString().trim();
        return normalized.isBlank() ? "file" : normalized;
    }

    private String extractExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot < 0 || lastDot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(lastDot);
    }

    private String normalizePrefix(String objectPrefix) {
        if (objectPrefix == null || objectPrefix.isBlank()) {
            return "";
        }

        String trimmed = objectPrefix.trim();
        while (trimmed.startsWith("/")) {
            trimmed = trimmed.substring(1);
        }
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    public record StoredObject(
        String bucket,
        String objectKey,
        String fileUrl,
        String fileName,
        String storedName,
        String contentType,
        Long fileSize
    ) {
    }
}
