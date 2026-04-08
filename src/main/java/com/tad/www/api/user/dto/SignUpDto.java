package com.tad.www.api.user.dto;

import java.time.OffsetDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignUpDto {
    private String email;
    private Boolean emailVerified;
    private String nickname;
    private String pictureUrl;
    private String status;


}
