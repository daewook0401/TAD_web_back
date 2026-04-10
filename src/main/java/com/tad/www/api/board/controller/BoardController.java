package com.tad.www.api.board.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.tad.www.api.board.dto.BoardCategoryResponse;
import com.tad.www.api.board.dto.BoardCommentCreateRequest;
import com.tad.www.api.board.dto.BoardCommentResponse;
import com.tad.www.api.board.dto.BoardCommentUpdateRequest;
import com.tad.www.api.board.dto.BoardLikeResponse;
import com.tad.www.api.board.dto.BoardPostCreateRequest;
import com.tad.www.api.board.dto.BoardPostDetailResponse;
import com.tad.www.api.board.dto.BoardPostListResponse;
import com.tad.www.api.board.dto.BoardPostUpdateRequest;
import com.tad.www.api.board.dto.SuccessResponse;
import com.tad.www.api.board.service.BoardCommentService;
import com.tad.www.api.board.service.BoardLikeService;
import com.tad.www.api.board.service.BoardService;
import com.tad.www.api.user.entity.User;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/board")
public class BoardController {

    private final BoardService boardService;
    private final BoardCommentService boardCommentService;
    private final BoardLikeService boardLikeService;

    @GetMapping("/categories")
    public ResponseEntity<List<BoardCategoryResponse>> getCategories() {
        return ResponseEntity.ok(boardService.getCategories());
    }

    @GetMapping("/posts")
    public ResponseEntity<BoardPostListResponse> getPosts(
        @RequestParam(required = false) String categoryKey,
        @RequestParam(required = false) String postType,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(boardService.getPosts(categoryKey, postType, page, size));
    }

    @GetMapping("/posts/{postId}")
    public ResponseEntity<BoardPostDetailResponse> getPostDetail(@PathVariable Long postId) {
        return ResponseEntity.ok(boardService.getPostDetail(postId));
    }

    @PostMapping("/posts")
    public ResponseEntity<BoardPostDetailResponse> createPost(
        @AuthenticationPrincipal User currentUser,
        @Valid @RequestPart("request") BoardPostCreateRequest request,
        @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        return ResponseEntity.ok(boardService.createPost(currentUser, request, files));
    }

    @PutMapping("/posts/{postId}")
    public ResponseEntity<BoardPostDetailResponse> updatePost(
        @PathVariable Long postId,
        @AuthenticationPrincipal User currentUser,
        @Valid @RequestPart("request") BoardPostUpdateRequest request,
        @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        return ResponseEntity.ok(boardService.updatePost(postId, currentUser, request, files));
    }

    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<SuccessResponse> deletePost(
        @PathVariable Long postId,
        @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(boardService.deletePost(postId, currentUser));
    }

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<BoardCommentResponse>> getComments(@PathVariable Long postId) {
        return ResponseEntity.ok(boardCommentService.getComments(postId));
    }

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<BoardCommentResponse> createComment(
        @PathVariable Long postId,
        @AuthenticationPrincipal User currentUser,
        @Valid @RequestPart("request") BoardCommentCreateRequest request,
        @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        return ResponseEntity.ok(boardCommentService.createComment(postId, currentUser, request, images));
    }

    @PutMapping("/comments/{commentId}")
    public ResponseEntity<BoardCommentResponse> updateComment(
        @PathVariable Long commentId,
        @AuthenticationPrincipal User currentUser,
        @Valid @RequestBody BoardCommentUpdateRequest request
    ) {
        return ResponseEntity.ok(boardCommentService.updateComment(commentId, currentUser, request));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<SuccessResponse> deleteComment(
        @PathVariable Long commentId,
        @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(boardCommentService.deleteComment(commentId, currentUser));
    }

    @PostMapping("/posts/{postId}/like")
    public ResponseEntity<BoardLikeResponse> likePost(
        @PathVariable Long postId,
        @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(boardLikeService.likePost(postId, currentUser));
    }

    @DeleteMapping("/posts/{postId}/like")
    public ResponseEntity<BoardLikeResponse> unlikePost(
        @PathVariable Long postId,
        @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(boardLikeService.unlikePost(postId, currentUser));
    }
}
