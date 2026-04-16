package com.tad.www.api.board.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.tad.www.api.auth.repository.UserRoleRepository;
import com.tad.www.api.board.dto.BoardCommentCreateRequest;
import com.tad.www.api.board.dto.BoardCommentResponse;
import com.tad.www.api.board.dto.BoardCommentUpdateRequest;
import com.tad.www.api.board.dto.SuccessResponse;
import com.tad.www.api.board.entity.BoardComment;
import com.tad.www.api.board.entity.BoardPost;
import com.tad.www.api.board.repository.BoardCommentRepository;
import com.tad.www.api.board.repository.BoardPostRepository;
import com.tad.www.api.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BoardCommentService {

    private final BoardCommentRepository boardCommentRepository;
    private final BoardPostRepository boardPostRepository;
    private final UserRoleRepository userRoleRepository;
    private final BoardAttachmentService boardAttachmentService;

    @Transactional(readOnly = true)
    public List<BoardCommentResponse> getComments(Long postId) {
        BoardPost post = findVisiblePost(postId);
        List<BoardComment> comments = boardCommentRepository.findByPostIdOrderByCreatedAtAscIdAsc(post.getId());
        Map<Long, BoardCommentResponse> mapped = new LinkedHashMap<>();
        List<BoardCommentResponse> roots = new ArrayList<>();

        for (BoardComment comment : comments) {
            BoardCommentResponse response = BoardCommentResponse.from(
                comment,
                boardAttachmentService.getCommentAttachments(comment.getId())
            );
            mapped.put(comment.getId(), response);

            BoardComment parent = comment.getParent();
            if (parent == null) {
                roots.add(response);
                continue;
            }

            BoardCommentResponse parentResponse = mapped.get(parent.getId());
            if (parentResponse == null) {
                roots.add(response);
                continue;
            }

            parentResponse.addReply(response);
        }

        return roots;
    }

    @Transactional
    public BoardCommentResponse createComment(
        Long postId,
        User currentUser,
        BoardCommentCreateRequest request,
        List<MultipartFile> images
    ) {
        BoardPost post = findVisiblePost(postId);
        BoardComment parent = resolveParent(postId, request.getParentId());
        String normalizedContent = normalizeContent(request.getContent());
        boolean hasImages = images != null && images.stream().anyMatch(file -> file != null && !file.isEmpty());

        if (normalizedContent == null && !hasImages) {
            throw new IllegalArgumentException("댓글 내용 또는 이미지가 하나 이상 필요합니다.");
        }

        BoardComment saved = boardCommentRepository.save(BoardComment.builder()
            .post(post)
            .author(currentUser)
            .parent(parent)
            .content(normalizedContent == null ? "" : normalizedContent)
            .isDeleted(false)
            .build());

        boardPostRepository.incrementReplyCount(postId);
        return BoardCommentResponse.from(
            saved,
            boardAttachmentService.storeCommentImages(saved, images)
        );
    }

    @Transactional
    public BoardCommentResponse updateComment(Long commentId, User currentUser, BoardCommentUpdateRequest request) {
        BoardComment comment = boardCommentRepository.findById(commentId)
            .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        if (Boolean.TRUE.equals(comment.getIsDeleted())) {
            throw new IllegalArgumentException("삭제된 댓글은 수정할 수 없습니다.");
        }

        ensureWritableUser(comment, currentUser);
        comment.setContent(request.getContent().trim());
        return BoardCommentResponse.from(
            comment,
            boardAttachmentService.getCommentAttachments(comment.getId())
        );
    }

    @Transactional
    public SuccessResponse deleteComment(Long commentId, User currentUser) {
        BoardComment comment = boardCommentRepository.findById(commentId)
            .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        ensureWritableUser(comment, currentUser);
        if (!Boolean.TRUE.equals(comment.getIsDeleted())) {
            comment.setIsDeleted(true);
            boardPostRepository.decrementReplyCount(comment.getPost().getId());
        }

        return SuccessResponse.builder()
            .success(true)
            .build();
    }

    private BoardPost findVisiblePost(Long postId) {
        return boardPostRepository.findByIdAndIsDeletedFalse(postId)
            .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
    }

    private BoardComment resolveParent(Long postId, Long parentId) {
        if (parentId == null) {
            return null;
        }

        BoardComment parent = boardCommentRepository.findById(parentId)
            .orElseThrow(() -> new IllegalArgumentException("부모 댓글을 찾을 수 없습니다."));

        if (!parent.getPost().getId().equals(postId)) {
            throw new IllegalArgumentException("부모 댓글이 다른 게시글에 속해 있습니다.");
        }

        return parent;
    }

    private void ensureWritableUser(BoardComment comment, User currentUser) {
        if (comment.getAuthor().getId().equals(currentUser.getId())) {
            return;
        }

        boolean isAdmin = userRoleRepository.findRoleNamesByUserId(currentUser.getId())
            .stream()
            .anyMatch("ROLE_ADMIN"::equals);

        if (!isAdmin) {
            throw new AccessDeniedException("댓글을 수정하거나 삭제할 권한이 없습니다.");
        }
    }

    private String normalizeContent(String content) {
        if (content == null) {
            return null;
        }
        String trimmed = content.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
