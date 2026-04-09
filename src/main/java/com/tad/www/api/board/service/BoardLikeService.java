package com.tad.www.api.board.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tad.www.api.board.dto.BoardLikeResponse;
import com.tad.www.api.board.entity.BoardPost;
import com.tad.www.api.board.entity.BoardPostLike;
import com.tad.www.api.board.repository.BoardPostLikeRepository;
import com.tad.www.api.board.repository.BoardPostRepository;
import com.tad.www.api.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BoardLikeService {

    private final BoardPostRepository boardPostRepository;
    private final BoardPostLikeRepository boardPostLikeRepository;

    @Transactional
    public BoardLikeResponse likePost(Long postId, User currentUser) {
        BoardPost post = findVisiblePost(postId);
        if (boardPostLikeRepository.existsByPostIdAndUserId(postId, currentUser.getId())) {
            return BoardLikeResponse.builder()
                .success(true)
                .liked(true)
                .likeCount(post.getLikeCount())
                .build();
        }

        boardPostLikeRepository.save(BoardPostLike.builder()
            .post(post)
            .user(currentUser)
            .build());
        boardPostRepository.incrementLikeCount(postId);

        return BoardLikeResponse.builder()
            .success(true)
            .liked(true)
            .likeCount(post.getLikeCount() + 1)
            .build();
    }

    @Transactional
    public BoardLikeResponse unlikePost(Long postId, User currentUser) {
        BoardPost post = findVisiblePost(postId);
        BoardPostLike like = boardPostLikeRepository.findByPostIdAndUserId(postId, currentUser.getId())
            .orElse(null);

        if (like == null) {
            return BoardLikeResponse.builder()
                .success(true)
                .liked(false)
                .likeCount(post.getLikeCount())
                .build();
        }

        boardPostLikeRepository.delete(like);
        boardPostRepository.decrementLikeCount(postId);

        return BoardLikeResponse.builder()
            .success(true)
            .liked(false)
            .likeCount(Math.max(post.getLikeCount() - 1, 0))
            .build();
    }

    private BoardPost findVisiblePost(Long postId) {
        return boardPostRepository.findByIdAndIsDeletedFalse(postId)
            .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
    }
}
