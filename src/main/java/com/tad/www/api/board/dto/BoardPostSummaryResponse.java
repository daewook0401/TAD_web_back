package com.tad.www.api.board.dto;

import java.time.LocalDateTime;

import com.tad.www.api.board.entity.BoardPost;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BoardPostSummaryResponse {

    private Long id;
    private String categoryKey;
    private String categoryName;
    private String title;
    private String tag;
    private String postType;
    private Integer viewCount;
    private Integer likeCount;
    private Integer replyCount;
    private Boolean notice;
    private Long authorId;
    private String authorNickname;
    private LocalDateTime createdAt;

    public static BoardPostSummaryResponse from(BoardPost post) {
        return BoardPostSummaryResponse.builder()
            .id(post.getId())
            .categoryKey(post.getCategory().getCategoryKey())
            .categoryName(post.getCategory().getName())
            .title(post.getTitle())
            .tag(post.getTag())
            .postType(normalizePostType(post.getPostType()))
            .viewCount(post.getViewCount())
            .likeCount(post.getLikeCount())
            .replyCount(post.getReplyCount())
            .notice(post.getIsNotice())
            .authorId(post.getAuthor().getId())
            .authorNickname(post.getAuthor().getNickname())
            .createdAt(post.getCreatedAt())
            .build();
    }

    private static String normalizePostType(String postType) {
        return postType == null ? null : postType.trim().toLowerCase();
    }
}
