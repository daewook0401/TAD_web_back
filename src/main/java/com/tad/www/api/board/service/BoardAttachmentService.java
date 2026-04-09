package com.tad.www.api.board.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.tad.www.api.board.dto.BoardAttachmentResponse;
import com.tad.www.api.board.entity.BoardComment;
import com.tad.www.api.board.entity.BoardCommentAttachment;
import com.tad.www.api.board.entity.BoardPost;
import com.tad.www.api.board.entity.BoardPostAttachment;
import com.tad.www.api.board.repository.BoardCommentAttachmentRepository;
import com.tad.www.api.board.repository.BoardPostAttachmentRepository;
import com.tad.www.core.config.minio.MinioProperties;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BoardAttachmentService {

    private static final String FILE_KIND_IMAGE = "image";
    private static final String FILE_KIND_FILE = "file";

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;
    private final BoardPostAttachmentRepository boardPostAttachmentRepository;
    private final BoardCommentAttachmentRepository boardCommentAttachmentRepository;

    @Transactional
    public List<BoardAttachmentResponse> storePostAttachments(BoardPost post, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return List.of();
        }

        ensureBucketExists();

        List<BoardAttachmentResponse> responses = new ArrayList<>();
        int sortOrder = 0;
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }

            StoredFile storedFile = uploadFile(file, "posts", post.getId());
            BoardPostAttachment saved = boardPostAttachmentRepository.save(BoardPostAttachment.builder()
                .post(post)
                .fileUrl(storedFile.fileUrl())
                .fileName(storedFile.fileName())
                .storedName(storedFile.storedName())
                .contentType(storedFile.contentType())
                .fileSize(storedFile.fileSize())
                .fileKind(storedFile.fileKind())
                .sortOrder(sortOrder++)
                .build());
            responses.add(BoardAttachmentResponse.from(saved));
        }

        return responses;
    }

    @Transactional
    public List<BoardAttachmentResponse> storeCommentImages(BoardComment comment, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return List.of();
        }

        ensureBucketExists();

        List<BoardAttachmentResponse> responses = new ArrayList<>();
        int sortOrder = 0;
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }

            StoredFile storedFile = uploadFile(file, "comments", comment.getId());
            if (!FILE_KIND_IMAGE.equals(storedFile.fileKind())) {
                throw new IllegalArgumentException("댓글 첨부는 이미지 파일만 업로드할 수 있습니다.");
            }

            BoardCommentAttachment saved = boardCommentAttachmentRepository.save(BoardCommentAttachment.builder()
                .comment(comment)
                .fileUrl(storedFile.fileUrl())
                .fileName(storedFile.fileName())
                .storedName(storedFile.storedName())
                .contentType(storedFile.contentType())
                .fileSize(storedFile.fileSize())
                .fileKind(storedFile.fileKind())
                .sortOrder(sortOrder++)
                .build());
            responses.add(BoardAttachmentResponse.from(saved));
        }

        return responses;
    }

    @Transactional(readOnly = true)
    public List<BoardAttachmentResponse> getPostAttachments(Long postId) {
        return boardPostAttachmentRepository.findByPostIdOrderBySortOrderAscIdAsc(postId)
            .stream()
            .map(BoardAttachmentResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<BoardAttachmentResponse> getCommentAttachments(Long commentId) {
        return boardCommentAttachmentRepository.findByCommentIdOrderBySortOrderAscIdAsc(commentId)
            .stream()
            .map(BoardAttachmentResponse::from)
            .toList();
    }

    private StoredFile uploadFile(MultipartFile file, String domain, Long ownerId) {
        String originalFileName = sanitizeFileName(file.getOriginalFilename());
        String extension = extractExtension(originalFileName);
        String storedName = UUID.randomUUID() + extension;
        String objectKey = "board/" + domain + "/" + ownerId + "/" + storedName;
        String contentType = normalizeContentType(file.getContentType(), originalFileName);
        String fileKind = contentType.startsWith("image/") ? FILE_KIND_IMAGE : FILE_KIND_FILE;

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
            throw new IllegalStateException("첨부파일 업로드에 실패했습니다.", e);
        }

        return new StoredFile(
            buildFileUrl(objectKey),
            originalFileName,
            storedName,
            contentType,
            file.getSize(),
            fileKind
        );
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

    private String normalizeContentType(String contentType, String fileName) {
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

    private record StoredFile(
        String fileUrl,
        String fileName,
        String storedName,
        String contentType,
        Long fileSize,
        String fileKind
    ) {
    }
}
