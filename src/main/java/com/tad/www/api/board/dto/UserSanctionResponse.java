package com.tad.www.api.board.dto;

import java.time.LocalDateTime;

import com.tad.www.api.board.entity.UserSanction;
import com.tad.www.api.user.entity.User;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserSanctionResponse {

    private Long id;
    private Long userId;
    private String userNickname;
    private String userEmail;
    private String sanctionType;
    private String reason;
    private LocalDateTime startsAt;
    private LocalDateTime expiresAt;
    private Long createdById;
    private String createdByNickname;
    private LocalDateTime createdAt;
    private LocalDateTime revokedAt;
    private Long revokedById;
    private String revokedByNickname;
    private String revokeReason;
    private Boolean active;

    public static UserSanctionResponse from(UserSanction sanction, LocalDateTime now) {
        User user = sanction.getUser();
        User createdBy = sanction.getCreatedBy();
        User revokedBy = sanction.getRevokedBy();

        return UserSanctionResponse.builder()
            .id(sanction.getId())
            .userId(user == null ? null : user.getId())
            .userNickname(user == null ? null : user.getNickname())
            .userEmail(user == null ? null : user.getEmail())
            .sanctionType(sanction.getSanctionType())
            .reason(sanction.getReason())
            .startsAt(sanction.getStartsAt())
            .expiresAt(sanction.getExpiresAt())
            .createdById(createdBy == null ? null : createdBy.getId())
            .createdByNickname(createdBy == null ? null : createdBy.getNickname())
            .createdAt(sanction.getCreatedAt())
            .revokedAt(sanction.getRevokedAt())
            .revokedById(revokedBy == null ? null : revokedBy.getId())
            .revokedByNickname(revokedBy == null ? null : revokedBy.getNickname())
            .revokeReason(sanction.getRevokeReason())
            .active(isActive(sanction, now))
            .build();
    }

    private static boolean isActive(UserSanction sanction, LocalDateTime now) {
        return sanction.getRevokedAt() == null
            && !sanction.getStartsAt().isAfter(now)
            && (sanction.getExpiresAt() == null || sanction.getExpiresAt().isAfter(now));
    }
}
