package com.tad.www.api.board.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tad.www.api.board.entity.BoardPostLike;

@Repository
public interface BoardPostLikeRepository extends JpaRepository<BoardPostLike, Long> {

    boolean existsByPostIdAndUserId(Long postId, Long userId);

    Optional<BoardPostLike> findByPostIdAndUserId(Long postId, Long userId);
}
