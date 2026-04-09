package com.tad.www.api.board.dto;

import com.tad.www.api.board.entity.BoardCommentAttachment;
import com.tad.www.api.board.entity.BoardPostAttachment;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BoardAttachmentResponse {

    private Long id;
    private String fileUrl;
    private String fileName;
    private String contentType;
    private Long fileSize;
    private String fileKind;
    private Integer sortOrder;

    public static BoardAttachmentResponse from(BoardPostAttachment attachment) {
        return BoardAttachmentResponse.builder()
            .id(attachment.getId())
            .fileUrl(attachment.getFileUrl())
            .fileName(attachment.getFileName())
            .contentType(attachment.getContentType())
            .fileSize(attachment.getFileSize())
            .fileKind(attachment.getFileKind())
            .sortOrder(attachment.getSortOrder())
            .build();
    }

    public static BoardAttachmentResponse from(BoardCommentAttachment attachment) {
        return BoardAttachmentResponse.builder()
            .id(attachment.getId())
            .fileUrl(attachment.getFileUrl())
            .fileName(attachment.getFileName())
            .contentType(attachment.getContentType())
            .fileSize(attachment.getFileSize())
            .fileKind(attachment.getFileKind())
            .sortOrder(attachment.getSortOrder())
            .build();
    }
}
