package com.tad.www.api.board.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tad.www.api.board.entity.BoardComment;

@Repository
public interface BoardCommentRepository extends JpaRepository<BoardComment, Long> {

    @EntityGraph(attributePaths = {"post", "author", "parent"})
    List<BoardComment> findByPostIdOrderByCreatedAtAscIdAsc(Long postId);
}
