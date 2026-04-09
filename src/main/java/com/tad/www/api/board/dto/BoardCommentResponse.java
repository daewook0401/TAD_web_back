package com.tad.www.api.board.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.tad.www.api.board.entity.BoardComment;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BoardCommentResponse {

    private Long id;
    private Long postId;
    private Long parentId;
    private String content;
    private Boolean deleted;
    private Long authorId;
    private String authorNickname;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<BoardAttachmentResponse> attachments;
    @Builder.Default
    private List<BoardCommentResponse> replies = new ArrayList<>();

    public static BoardCommentResponse from(BoardComment comment, List<BoardAttachmentResponse> attachments) {
        return BoardCommentResponse.builder()
            .id(comment.getId())
            .postId(comment.getPost().getId())
            .parentId(comment.getParent() == null ? null : comment.getParent().getId())
            .content(comment.getIsDeleted() ? "[deleted]" : comment.getContent())
            .deleted(comment.getIsDeleted())
            .authorId(comment.getAuthor().getId())
            .authorNickname(comment.getIsDeleted() ? null : comment.getAuthor().getNickname())
            .createdAt(comment.getCreatedAt())
            .updatedAt(comment.getUpdatedAt())
            .attachments(attachments)
            .build();
    }

    public void addReply(BoardCommentResponse reply) {
        replies.add(reply);
    }
}
