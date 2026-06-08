package com.tad.www.api.board.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSanctionCreateRequest {

    @NotNull(message = "제재 대상 회원 ID는 필수입니다.")
    private Long userId;

    private String sanctionType;

    private Integer sanctionDays;

    @Size(max = 1000, message = "제재 사유는 1000자 이하로 입력해주세요.")
    private String reason;
}
