package com.tad.www.api.board.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tad.www.api.board.entity.BoardComment;

@Repository
public interface BoardCommentRepository extends JpaRepository<BoardComment, Long> {

    @EntityGraph(attributePaths = {"post", "author", "parent"})
    List<BoardComment> findByPostIdOrderByCreatedAtAscIdAsc(Long postId);

    @EntityGraph(attributePaths = {"post", "post.category", "author", "parent"})
    Optional<BoardComment> findByIdAndIsDeletedFalse(Long id);

    @EntityGraph(attributePaths = {"post", "post.category"})
    @Query("""
        select c
        from BoardComment c
        where c.author.id = :authorId
          and c.isDeleted = false
          and c.post.isDeleted = false
        order by c.createdAt desc, c.id desc
        """)
    List<BoardComment> findRecentVisibleByAuthorId(
        @Param("authorId") Long authorId,
        Pageable pageable
    );

    @Query("""
        select count(c)
        from BoardComment c
        where c.author.id = :authorId
          and c.isDeleted = false
          and c.post.isDeleted = false
        """)
    long countVisibleByAuthorId(@Param("authorId") Long authorId);
}
