package com.tad.www.api.board.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tad.www.api.board.dto.BoardCategoryResponse;
import com.tad.www.api.board.dto.BoardPostDetailResponse;
import com.tad.www.api.board.dto.BoardPostListResponse;
import com.tad.www.api.board.service.BoardService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/board")
public class BoardController {

    private final BoardService boardService;

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
}
