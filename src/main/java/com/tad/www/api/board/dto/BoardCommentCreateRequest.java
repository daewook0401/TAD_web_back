package com.tad.www.api.board.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoardCommentCreateRequest {

    private Long parentId;

    private String content;
}
