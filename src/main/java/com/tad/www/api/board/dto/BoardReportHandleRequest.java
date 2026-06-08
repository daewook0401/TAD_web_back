package com.tad.www.api.board.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoardReportHandleRequest {

    @NotBlank(message = "처리 상태는 필수입니다.")
    private String status;

    @Size(max = 1000, message = "처리 메모는 1000자 이하로 입력해주세요.")
    private String handlerMemo;

    private String sanctionType;

    private Integer sanctionDays;

    @Size(max = 1000, message = "제재 사유는 1000자 이하로 입력해주세요.")
    private String sanctionReason;
}
