package com.tad.www.api.board.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tad.www.api.board.dto.BoardLikeResponse;
import com.tad.www.api.board.entity.BoardCategory;
import com.tad.www.api.board.entity.BoardPost;
import com.tad.www.api.board.entity.BoardPostLike;
import com.tad.www.api.board.repository.BoardPostLikeRepository;
import com.tad.www.api.board.repository.BoardPostRepository;
import com.tad.www.api.user.entity.User;

@ExtendWith(MockitoExtension.class)
class BoardLikeServiceTest {

    @Mock
    private BoardPostRepository boardPostRepository;

    @Mock
    private BoardPostLikeRepository boardPostLikeRepository;

    @InjectMocks
    private BoardLikeService boardLikeService;

    @Test
    void likePostCreatesLikeAndIncrementsCount() {
        BoardPost post = boardPost(10L, 2);
        User user = user(3L);

        when(boardPostRepository.findByIdAndIsDeletedFalse(10L)).thenReturn(Optional.of(post));
        when(boardPostLikeRepository.existsByPostIdAndUserId(10L, 3L)).thenReturn(false);

        BoardLikeResponse response = boardLikeService.likePost(10L, user);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.isLiked()).isTrue();
        assertThat(response.getLikeCount()).isEqualTo(3);
        verify(boardPostLikeRepository).save(org.mockito.ArgumentMatchers.any(BoardPostLike.class));
        verify(boardPostRepository).incrementLikeCount(10L);
    }

    @Test
    void unlikePostDeletesExistingLikeAndDecrementsCount() {
        BoardPost post = boardPost(10L, 2);
        User user = user(3L);
        BoardPostLike like = BoardPostLike.builder()
            .id(1L)
            .post(post)
            .user(user)
            .build();

        when(boardPostRepository.findByIdAndIsDeletedFalse(10L)).thenReturn(Optional.of(post));
        when(boardPostLikeRepository.findByPostIdAndUserId(10L, 3L)).thenReturn(Optional.of(like));

        BoardLikeResponse response = boardLikeService.unlikePost(10L, user);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.isLiked()).isFalse();
        assertThat(response.getLikeCount()).isEqualTo(1);
        verify(boardPostLikeRepository).delete(like);
        verify(boardPostRepository).decrementLikeCount(10L);
    }

    @Test
    void likePostFailsWhenPostDoesNotExist() {
        when(boardPostRepository.findByIdAndIsDeletedFalse(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> boardLikeService.likePost(99L, user(3L)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("게시글을 찾을 수 없습니다.");

        verify(boardPostLikeRepository, never()).save(org.mockito.ArgumentMatchers.any(BoardPostLike.class));
    }

    private BoardPost boardPost(Long id, int likeCount) {
        return BoardPost.builder()
            .id(id)
            .category(BoardCategory.builder().id(1L).categoryKey("free").name("Free").build())
            .author(user(1L))
            .title("title")
            .content("content")
            .postType("free")
            .viewCount(0)
            .likeCount(likeCount)
            .replyCount(0)
            .isNotice(false)
            .isDeleted(false)
            .build();
    }

    private User user(Long id) {
        return User.builder()
            .id(id)
            .email("user@example.com")
            .nickname("user")
            .status("ACTIVE")
            .build();
    }
}
