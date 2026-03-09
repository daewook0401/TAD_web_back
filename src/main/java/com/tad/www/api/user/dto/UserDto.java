package com.tad.www.api.user.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.tad.www.api.user.entity.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private Long id;
    private UUID publicId;
    private String email;
    private String nickname;
    private String pictureUrl;
    private String status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private OffsetDateTime lastLoginAt;

    public static UserDto from(User u) {
        if (u == null) return null;
        return UserDto.builder()
                .id(u.getId())
                .publicId(u.getPublicId())
                .email(u.getEmail())
                .nickname(u.getNickname())
                .pictureUrl(u.getPictureUrl())
                .status(u.getStatus())
                .createdAt(u.getCreatedAt())
                .updatedAt(u.getUpdatedAt())
                .lastLoginAt(u.getLastLoginAt())
                .build();
    }
}
