package com.tad.www.api.board.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BoardLikeResponse {

    private boolean success;
    private boolean liked;
    private int likeCount;
}
