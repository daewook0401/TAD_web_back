package com.tad.www.core.config.minio;

import java.io.InputStream;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

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
            throw new IllegalStateException("S3 파일 업로드에 실패했습니다.", e);
        }

        return new StoredObject(
            minioProperties.getBucket(),
            objectKey,
            buildPublicFileUrl(minioProperties.getBucket(), objectKey),
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

    public String buildPublicFileUrl(String bucket, String objectKey) {
        String normalizedBucket = requireValue(bucket, "bucket");
        String normalizedObjectKey = requireValue(objectKey, "objectKey");
        String drivePublicUrl = normalizeBaseUrl(minioProperties.getDrivePublicUrl());
        if (drivePublicUrl == null) {
            return buildLegacyFileUrl(normalizedBucket, normalizedObjectKey);
        }

        return drivePublicUrl
            + "/public/"
            + UriUtils.encodePathSegment(normalizedBucket, StandardCharsets.UTF_8)
            + "/"
            + encodeObjectKey(normalizedObjectKey);
    }

    private String buildLegacyFileUrl(String bucket, String objectKey) {
        String baseUrl = normalizeBaseUrl(minioProperties.getPublicUrl());
        if (baseUrl == null) {
            baseUrl = normalizeBaseUrl(minioProperties.getEndpoint());
        }
        if (baseUrl == null) {
            throw new IllegalStateException("공개 파일 URL base 설정이 필요합니다.");
        }
        return baseUrl
            + "/"
            + UriUtils.encodePathSegment(bucket, StandardCharsets.UTF_8)
            + "/"
            + encodeObjectKey(objectKey);
    }

    private String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return null;
        }
        String normalized = baseUrl.trim();
        return normalized.endsWith("/") ? normalized.substring(0, normalized.length() - 1) : normalized;
    }

    private String encodeObjectKey(String objectKey) {
        return Arrays.stream(objectKey.split("/", -1))
            .map(segment -> UriUtils.encodePathSegment(segment, StandardCharsets.UTF_8))
            .collect(Collectors.joining("/"));
    }

    private String requireValue(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " 값이 필요합니다.");
        }
        return value.trim();
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
