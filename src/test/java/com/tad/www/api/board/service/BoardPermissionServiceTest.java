package com.tad.www.api.board.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.tad.www.api.auth.repository.UserRoleRepository;
import com.tad.www.api.board.entity.BoardCategory;
import com.tad.www.api.board.entity.BoardComment;
import com.tad.www.api.board.entity.BoardPost;
import com.tad.www.api.user.entity.User;

@ExtendWith(MockitoExtension.class)
class BoardPermissionServiceTest {

    @Mock
    private UserRoleRepository userRoleRepository;

    @InjectMocks
    private BoardPermissionService boardPermissionService;

    @Test
    void ensurePostWritableAllowsAuthorWithoutRoleLookup() {
        BoardPost post = boardPost(user(1L));

        assertThatCode(() -> boardPermissionService.ensurePostWritable(post, user(1L)))
            .doesNotThrowAnyException();

        verify(userRoleRepository, never()).findRoleNamesByUserId(1L);
    }

    @Test
    void ensurePostWritableAllowsAdmin() {
        when(userRoleRepository.findRoleNamesByUserId(2L)).thenReturn(List.of("ROLE_ADMIN"));

        assertThatCode(() -> boardPermissionService.ensurePostWritable(boardPost(user(1L)), user(2L)))
            .doesNotThrowAnyException();
    }

    @Test
    void ensureCommentWritableRejectsNonAuthorNonAdmin() {
        when(userRoleRepository.findRoleNamesByUserId(2L)).thenReturn(List.of("ROLE_USER"));

        assertThatThrownBy(() -> boardPermissionService.ensureCommentWritable(comment(user(1L)), user(2L)))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessage("댓글을 수정하거나 삭제할 권한이 없습니다.");
    }

    private BoardPost boardPost(User author) {
        return BoardPost.builder()
            .id(10L)
            .category(BoardCategory.builder().id(1L).categoryKey("free").name("Free").build())
            .author(author)
            .title("title")
            .content("content")
            .postType("free")
            .viewCount(0)
            .likeCount(0)
            .replyCount(0)
            .isNotice(false)
            .isDeleted(false)
            .build();
    }

    private BoardComment comment(User author) {
        return BoardComment.builder()
            .id(20L)
            .post(boardPost(author))
            .author(author)
            .content("comment")
            .isDeleted(false)
            .build();
    }

    private User user(Long id) {
        return User.builder()
            .id(id)
            .email("user" + id + "@example.com")
            .nickname("user" + id)
            .status("ACTIVE")
            .build();
    }
}
