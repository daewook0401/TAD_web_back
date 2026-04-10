package com.tad.www.api.board.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoardPostUpdateRequest {

    @NotNull(message = "카테고리는 필수입니다.")
    private Long categoryId;

    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 200, message = "제목은 200자 이하여야 합니다.")
    private String title;

    @NotBlank(message = "내용은 필수입니다.")
    private String content;

    @Size(max = 50, message = "태그는 50자 이하여야 합니다.")
    private String tag;

    @NotBlank(message = "게시글 타입은 필수입니다.")
    private String postType;

    private Boolean notice;
}
