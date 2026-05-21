package com.tad.www.api.board.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.tad.www.api.auth.repository.UserRoleRepository;
import com.tad.www.api.board.entity.BoardComment;
import com.tad.www.api.board.entity.BoardPost;
import com.tad.www.api.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BoardPermissionService {

    private final UserRoleRepository userRoleRepository;

    public void ensurePostWritable(BoardPost post, User currentUser) {
        if (!canManage(post.getAuthor().getId(), currentUser)) {
            throw new AccessDeniedException("게시글을 수정하거나 삭제할 권한이 없습니다.");
        }
    }

    public void ensureCommentWritable(BoardComment comment, User currentUser) {
        if (!canManage(comment.getAuthor().getId(), currentUser)) {
            throw new AccessDeniedException("댓글을 수정하거나 삭제할 권한이 없습니다.");
        }
    }

    private boolean canManage(Long authorId, User currentUser) {
        if (currentUser == null || currentUser.getId() == null) {
            return false;
        }

        if (authorId != null && authorId.equals(currentUser.getId())) {
            return true;
        }

        return userRoleRepository.findRoleNamesByUserId(currentUser.getId())
            .stream()
            .anyMatch("ROLE_ADMIN"::equals);
    }
}
