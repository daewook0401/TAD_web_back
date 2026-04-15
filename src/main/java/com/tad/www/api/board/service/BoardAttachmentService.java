package com.tad.www.api.board.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
import com.tad.www.core.config.minio.MinioStorageService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BoardAttachmentService {

    private static final String FILE_KIND_IMAGE = "image";
    private static final String FILE_KIND_FILE = "file";

    private final MinioStorageService minioStorageService;
    private final BoardPostAttachmentRepository boardPostAttachmentRepository;
    private final BoardCommentAttachmentRepository boardCommentAttachmentRepository;

    @Transactional
    public List<BoardAttachmentResponse> storePostAttachments(BoardPost post, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return List.of();
        }

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

        List<BoardAttachmentResponse> responses = new ArrayList<>();
        int sortOrder = 0;
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }

            StoredFile storedFile = uploadFile(file, "comments", comment.getId());
            if (!FILE_KIND_IMAGE.equals(storedFile.fileKind())) {
                throw new IllegalArgumentException("댓글 첨부에는 이미지 파일만 업로드할 수 있습니다.");
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
        MinioStorageService.StoredObject storedObject = minioStorageService.store(file, "board/" + domain + "/" + ownerId);
        String contentType = normalizeContentType(storedObject.contentType(), storedObject.fileName());
        String fileKind = contentType.startsWith("image/") ? FILE_KIND_IMAGE : FILE_KIND_FILE;

        return new StoredFile(
            storedObject.fileUrl(),
            storedObject.fileName(),
            storedObject.storedName(),
            contentType,
            storedObject.fileSize(),
            fileKind
        );
    }

    private String normalizeContentType(String contentType, String fileName) {
        return minioStorageService.normalizeContentType(contentType, fileName).toLowerCase(Locale.ROOT);
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
