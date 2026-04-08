package com.tad.www.api.board.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tad.www.api.board.entity.BoardPost;

@Repository
public interface BoardPostRepository extends JpaRepository<BoardPost, Long> {

    @EntityGraph(attributePaths = {"category", "author"})
    @Query("""
        select p
        from BoardPost p
        where p.isDeleted = false
          and (:categoryKey is null or p.category.categoryKey = :categoryKey)
          and (:postType is null or lower(p.postType) = lower(:postType))
        """)
    Page<BoardPost> findVisiblePosts(
        @Param("categoryKey") String categoryKey,
        @Param("postType") String postType,
        Pageable pageable
    );

    @EntityGraph(attributePaths = {"category", "author"})
    Optional<BoardPost> findByIdAndIsDeletedFalse(Long id);
}
