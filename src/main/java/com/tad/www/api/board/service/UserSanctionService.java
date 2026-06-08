package com.tad.www.api.board.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tad.www.api.board.dto.UserSanctionCreateRequest;
import com.tad.www.api.board.dto.UserSanctionRevokeRequest;
import com.tad.www.api.board.dto.UserSanctionResponse;
import com.tad.www.api.board.entity.UserSanction;
import com.tad.www.api.board.repository.UserSanctionRepository;
import com.tad.www.api.user.entity.User;
import com.tad.www.api.user.repository.UserRepository;
import com.tad.www.core.util.TextUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserSanctionService {

    public static final String TYPE_SUSPENDED = "SUSPENDED";
    public static final String TYPE_BANNED = "BANNED";

    private static final int DEFAULT_SUSPEND_DAYS = 7;
    private static final int MAX_SUSPEND_DAYS = 3650;
    private static final int DEFAULT_ADMIN_SANCTION_LIMIT = 100;
    private static final int MAX_ADMIN_SANCTION_LIMIT = 300;

    private final UserSanctionRepository userSanctionRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public void ensureBoardWritable(User currentUser) {
        if (currentUser == null || currentUser.getId() == null) {
            throw new AccessDeniedException("로그인이 필요합니다.");
        }

        LocalDateTime now = LocalDateTime.now();
        List<UserSanction> sanctions = userSanctionRepository.findActiveByUserId(
            currentUser.getId(),
            now,
            PageRequest.of(0, 1)
        );
        if (sanctions.isEmpty()) {
            return;
        }

        UserSanction sanction = sanctions.get(0);
        if (TYPE_BANNED.equals(sanction.getSanctionType())) {
            throw new AccessDeniedException("게시판 이용이 영구 제한된 계정입니다.");
        }

        throw new AccessDeniedException("게시판 이용이 일시 제한된 계정입니다.");
    }

    @Transactional(readOnly = true)
    public List<UserSanctionResponse> getAdminSanctions(Long userId, Boolean activeOnly, Integer limit) {
        LocalDateTime now = LocalDateTime.now();
        int normalizedLimit = normalizeLimit(limit);
        return userSanctionRepository.findAdminSanctions(
                userId,
                Boolean.TRUE.equals(activeOnly),
                now,
                PageRequest.of(0, normalizedLimit)
            )
            .stream()
            .map(sanction -> UserSanctionResponse.from(sanction, now))
            .toList();
    }

    @Transactional
    public UserSanctionResponse createSanction(User currentUser, UserSanctionCreateRequest request) {
        return createSanction(
            request.getUserId(),
            request.getSanctionType(),
            request.getSanctionDays(),
            request.getReason(),
            currentUser
        );
    }

    @Transactional
    public UserSanctionResponse createSanction(
        Long userId,
        String sanctionType,
        Integer sanctionDays,
        String reason,
        User currentUser
    ) {
        if (userId == null) {
            throw new IllegalArgumentException("제재 대상 회원 ID는 필수입니다.");
        }

        User targetUser = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("제재 대상 회원을 찾을 수 없습니다."));

        String normalizedType = normalizeSanctionType(sanctionType);
        String normalizedReason = TextUtils.normalizeNullable(reason);
        if (normalizedReason == null) {
            throw new IllegalArgumentException("제재 사유는 필수입니다.");
        }

        LocalDateTime now = LocalDateTime.now();
        UserSanction saved = userSanctionRepository.save(UserSanction.builder()
            .user(targetUser)
            .sanctionType(normalizedType)
            .reason(normalizedReason)
            .startsAt(now)
            .expiresAt(resolveExpiresAt(normalizedType, sanctionDays, now))
            .createdBy(currentUser)
            .build());

        return UserSanctionResponse.from(saved, now);
    }

    @Transactional
    public UserSanctionResponse revokeSanction(Long sanctionId, User currentUser, UserSanctionRevokeRequest request) {
        UserSanction sanction = userSanctionRepository.findById(sanctionId)
            .orElseThrow(() -> new IllegalArgumentException("제재 정보를 찾을 수 없습니다."));

        if (sanction.getRevokedAt() == null) {
            sanction.setRevokedAt(LocalDateTime.now());
            sanction.setRevokedBy(currentUser);
            sanction.setRevokeReason(TextUtils.normalizeNullable(request == null ? null : request.getReason()));
        }

        return UserSanctionResponse.from(sanction, LocalDateTime.now());
    }

    private String normalizeSanctionType(String sanctionType) {
        String normalized = TextUtils.normalizeNullable(sanctionType);
        normalized = normalized == null ? TYPE_SUSPENDED : normalized.toUpperCase(Locale.ROOT);

        if (!TYPE_SUSPENDED.equals(normalized) && !TYPE_BANNED.equals(normalized)) {
            throw new IllegalArgumentException("지원하지 않는 제재 유형입니다.");
        }

        return normalized;
    }

    private LocalDateTime resolveExpiresAt(String sanctionType, Integer sanctionDays, LocalDateTime now) {
        if (TYPE_BANNED.equals(sanctionType)) {
            return null;
        }

        int days = sanctionDays == null || sanctionDays < 1
            ? DEFAULT_SUSPEND_DAYS
            : Math.min(sanctionDays, MAX_SUSPEND_DAYS);
        return now.plusDays(days);
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit < 1) {
            return DEFAULT_ADMIN_SANCTION_LIMIT;
        }
        return Math.min(limit, MAX_ADMIN_SANCTION_LIMIT);
    }
}
