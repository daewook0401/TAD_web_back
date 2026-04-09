package com.tad.www.api.board.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.tad.www.api.board.entity.BoardPost;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BoardPostDetailResponse {

    private Long id;
    private Long categoryId;
    private String categoryKey;
    private String categoryName;
    private String title;
    private String content;
    private String tag;
    private String postType;
    private Integer viewCount;
    private Integer likeCount;
    private Integer replyCount;
    private Boolean notice;
    private Long authorId;
    private String authorNickname;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<BoardAttachmentResponse> attachments;

    public static BoardPostDetailResponse from(BoardPost post, List<BoardAttachmentResponse> attachments) {
        return BoardPostDetailResponse.builder()
            .id(post.getId())
            .categoryId(post.getCategory().getId())
            .categoryKey(post.getCategory().getCategoryKey())
            .categoryName(post.getCategory().getName())
            .title(post.getTitle())
            .content(post.getContent())
            .tag(post.getTag())
            .postType(normalizePostType(post.getPostType()))
            .viewCount(post.getViewCount())
            .likeCount(post.getLikeCount())
            .replyCount(post.getReplyCount())
            .notice(post.getIsNotice())
            .authorId(post.getAuthor().getId())
            .authorNickname(post.getAuthor().getNickname())
            .createdAt(post.getCreatedAt())
            .updatedAt(post.getUpdatedAt())
            .attachments(attachments)
            .build();
    }

    private static String normalizePostType(String postType) {
        return postType == null ? null : postType.trim().toLowerCase();
    }
}
