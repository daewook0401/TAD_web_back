package com.tad.www.api.board.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoardReportCreateRequest {

    @NotBlank(message = "신고 대상 유형은 필수입니다.")
    private String targetType;

    @NotNull(message = "신고 대상 ID는 필수입니다.")
    private Long targetId;

    @NotBlank(message = "신고 사유는 필수입니다.")
    @Size(max = 50, message = "신고 사유 코드는 50자 이하로 입력해주세요.")
    private String reasonCode;

    @Size(max = 1000, message = "상세 사유는 1000자 이하로 입력해주세요.")
    private String reasonDetail;
}
