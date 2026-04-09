package com.tad.www.api.board.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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
          and (:postType is null or lower(p.postType) = :postType)
        """)
    Page<BoardPost> findVisiblePosts(
        @Param("categoryKey") String categoryKey,
        @Param("postType") String postType,
        Pageable pageable
    );

    @EntityGraph(attributePaths = {"category", "author"})
    Optional<BoardPost> findByIdAndIsDeletedFalse(Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update BoardPost p
           set p.viewCount = p.viewCount + 1
         where p.id = :id
           and p.isDeleted = false
        """)
    int incrementViewCount(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update BoardPost p
           set p.replyCount = p.replyCount + 1
         where p.id = :id
           and p.isDeleted = false
        """)
    int incrementReplyCount(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update BoardPost p
           set p.replyCount = case when p.replyCount > 0 then p.replyCount - 1 else 0 end
         where p.id = :id
           and p.isDeleted = false
        """)
    int decrementReplyCount(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update BoardPost p
           set p.likeCount = p.likeCount + 1
         where p.id = :id
           and p.isDeleted = false
        """)
    int incrementLikeCount(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update BoardPost p
           set p.likeCount = case when p.likeCount > 0 then p.likeCount - 1 else 0 end
         where p.id = :id
           and p.isDeleted = false
        """)
    int decrementLikeCount(@Param("id") Long id);
}
