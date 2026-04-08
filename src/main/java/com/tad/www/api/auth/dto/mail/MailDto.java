package com.tad.www.api.auth.dto.mail;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MailDto {
    private String code;
    private UUID requestId;
    private String email;
    private String purpose;

}
