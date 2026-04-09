package com.tad.www.api.board.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.tad.www.api.board.dto.BoardCategoryResponse;
import com.tad.www.api.board.dto.BoardPostCreateRequest;
import com.tad.www.api.board.dto.BoardPostDetailResponse;
import com.tad.www.api.board.dto.BoardPostListResponse;
import com.tad.www.api.board.dto.BoardPostSummaryResponse;
import com.tad.www.api.board.entity.BoardCategory;
import com.tad.www.api.board.entity.BoardPost;
import com.tad.www.api.board.repository.BoardCategoryRepository;
import com.tad.www.api.board.repository.BoardPostRepository;
import com.tad.www.api.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardCategoryRepository boardCategoryRepository;
    private final BoardPostRepository boardPostRepository;
    private final BoardAttachmentService boardAttachmentService;

    @Transactional(readOnly = true)
    public List<BoardCategoryResponse> getCategories() {
        return boardCategoryRepository.findAllByOrderByDisplayOrderAscIdAsc()
            .stream()
            .map(BoardCategoryResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public BoardPostListResponse getPosts(String categoryKey, String postType, int page, int size) {
        String normalizedCategoryKey = normalize(categoryKey);
        String normalizedPostType = normalizePostType(postType);

        PageRequest pageable = PageRequest.of(
            page,
            size,
            Sort.by(
                Sort.Order.desc("isNotice"),
                Sort.Order.asc("postType"),
                Sort.Order.desc("createdAt")
            )
        );

        Page<BoardPost> posts = boardPostRepository.findVisiblePosts(
            normalizedCategoryKey,
            normalizedPostType,
            pageable
        );

        return BoardPostListResponse.builder()
            .items(posts.getContent().stream().map(BoardPostSummaryResponse::from).toList())
            .page(posts.getNumber())
            .size(posts.getSize())
            .totalElements(posts.getTotalElements())
            .totalPages(posts.getTotalPages())
            .hasNext(posts.hasNext())
            .build();
    }

    @Transactional
    public BoardPostDetailResponse getPostDetail(Long postId) {
        if (boardPostRepository.incrementViewCount(postId) == 0) {
            throw new IllegalArgumentException("게시글을 찾을 수 없습니다.");
        }

        BoardPost post = boardPostRepository.findByIdAndIsDeletedFalse(postId)
            .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        return BoardPostDetailResponse.from(post, boardAttachmentService.getPostAttachments(postId));
    }

    @Transactional
    public BoardPostDetailResponse createPost(User currentUser, BoardPostCreateRequest request, List<MultipartFile> files) {
        BoardCategory category = boardCategoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다."));

        String normalizedPostType = normalizePostType(request.getPostType());
        BoardPost post = boardPostRepository.save(BoardPost.builder()
            .category(category)
            .author(currentUser)
            .title(request.getTitle().trim())
            .content(request.getContent().trim())
            .tag(normalizeNullableText(request.getTag()))
            .postType(normalizedPostType)
            .viewCount(0)
            .likeCount(0)
            .replyCount(0)
            .isNotice(Boolean.TRUE.equals(request.getNotice()))
            .isDeleted(false)
            .build());

        return BoardPostDetailResponse.from(
            post,
            boardAttachmentService.storePostAttachments(post, files)
        );
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim().toLowerCase();
    }

    private String normalizePostType(String postType) {
        String normalized = normalize(postType);
        if (normalized == null) {
            throw new IllegalArgumentException("게시글 타입은 필수입니다.");
        }
        if (!"free".equals(normalized) && !"info".equals(normalized)) {
            throw new IllegalArgumentException("지원하지 않는 게시글 타입입니다.");
        }
        return normalized;
    }

    private String normalizeNullableText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
