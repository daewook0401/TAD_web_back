package com.tad.www.api.board.dto;

import com.tad.www.api.board.entity.BoardCategory;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BoardCategoryResponse {

    private Long id;
    private String categoryKey;
    private String name;
    private String iconUrl;
    private String summary;
    private Integer displayOrder;

    public static BoardCategoryResponse from(BoardCategory category) {
        return BoardCategoryResponse.builder()
            .id(category.getId())
            .categoryKey(category.getCategoryKey())
            .name(category.getName())
            .iconUrl(category.getIconUrl())
            .summary(category.getSummary())
            .displayOrder(category.getDisplayOrder())
            .build();
    }
}
