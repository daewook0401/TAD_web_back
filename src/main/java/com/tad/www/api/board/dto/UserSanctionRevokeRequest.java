package com.tad.www.api.board.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSanctionRevokeRequest {

    @Size(max = 1000, message = "해제 사유는 1000자 이하로 입력해주세요.")
    private String reason;
}
