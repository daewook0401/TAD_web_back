package com.tad.www.api.board.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.tad.www.api.auth.repository.UserRoleRepository;
import com.tad.www.api.board.dto.BoardCommentCreateRequest;
import com.tad.www.api.board.dto.BoardCommentResponse;
import com.tad.www.api.board.dto.BoardCommentUpdateRequest;
import com.tad.www.api.board.entity.BoardCategory;
import com.tad.www.api.board.entity.BoardComment;
import com.tad.www.api.board.entity.BoardPost;
import com.tad.www.api.board.repository.BoardCommentRepository;
import com.tad.www.api.board.repository.BoardPostRepository;
import com.tad.www.api.user.entity.User;

@ExtendWith(MockitoExtension.class)
class BoardCommentServiceTest {

    @Mock
    private BoardCommentRepository boardCommentRepository;

    @Mock
    private BoardPostRepository boardPostRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private BoardAttachmentService boardAttachmentService;

    @InjectMocks
    private BoardCommentService boardCommentService;

    @Test
    void getCommentsBuildsReplyTree() {
        BoardPost post = boardPost(10L);
        BoardComment parent = comment(1L, post, user(1L), null, "parent", false);
        BoardComment child = comment(2L, post, user(2L), parent, "child", false);

        when(boardPostRepository.findByIdAndIsDeletedFalse(10L)).thenReturn(Optional.of(post));
        when(boardCommentRepository.findByPostIdOrderByCreatedAtAscIdAsc(10L)).thenReturn(List.of(parent, child));
        when(boardAttachmentService.getCommentAttachments(1L)).thenReturn(List.of());
        when(boardAttachmentService.getCommentAttachments(2L)).thenReturn(List.of());

        List<BoardCommentResponse> responses = boardCommentService.getComments(10L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getReplies()).hasSize(1);
        assertThat(responses.get(0).getReplies().get(0).getId()).isEqualTo(2L);
    }

    @Test
    void createCommentIncrementsReplyCount() {
        BoardPost post = boardPost(10L);
        User user = user(3L);
        BoardComment saved = comment(11L, post, user, null, "hello", false);
        BoardCommentCreateRequest request = new BoardCommentCreateRequest();
        request.setContent(" hello ");

        when(boardPostRepository.findByIdAndIsDeletedFalse(10L)).thenReturn(Optional.of(post));
        when(boardCommentRepository.save(org.mockito.ArgumentMatchers.any(BoardComment.class))).thenReturn(saved);
        when(boardAttachmentService.storeCommentImages(saved, null)).thenReturn(List.of());

        BoardCommentResponse response = boardCommentService.createComment(10L, user, request, null);

        assertThat(response.getId()).isEqualTo(11L);
        verify(boardPostRepository).incrementReplyCount(10L);
    }

    @Test
    void updateCommentRejectsNonAuthorNonAdmin() {
        BoardPost post = boardPost(10L);
        BoardComment comment = comment(1L, post, user(1L), null, "text", false);
        BoardCommentUpdateRequest request = new BoardCommentUpdateRequest();
        request.setContent("new");

        when(boardCommentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(userRoleRepository.findRoleNamesByUserId(2L)).thenReturn(List.of("ROLE_USER"));

        assertThatThrownBy(() -> boardCommentService.updateComment(1L, user(2L), request))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessage("댓글을 수정 또는 삭제할 권한이 없습니다.");
    }

    private BoardPost boardPost(Long id) {
        return BoardPost.builder()
            .id(id)
            .category(BoardCategory.builder().id(1L).categoryKey("free").name("Free").build())
            .author(user(1L))
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

    private BoardComment comment(Long id, BoardPost post, User author, BoardComment parent, String content, boolean deleted) {
        return BoardComment.builder()
            .id(id)
            .post(post)
            .author(author)
            .parent(parent)
            .content(content)
            .isDeleted(deleted)
            .createdAt(LocalDateTime.of(2026, 4, 9, 12, 0))
            .updatedAt(LocalDateTime.of(2026, 4, 9, 12, 0))
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
