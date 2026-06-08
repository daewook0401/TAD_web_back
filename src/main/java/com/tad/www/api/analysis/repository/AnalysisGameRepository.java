package com.tad.www.api.analysis.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tad.www.api.analysis.entity.AnalysisGame;

@Repository
public interface AnalysisGameRepository extends JpaRepository<AnalysisGame, Long> {

    @EntityGraph(attributePaths = {"uploader"})
    List<AnalysisGame> findByUploaderIdOrderByCreatedAtDesc(Long uploaderId);

    @EntityGraph(attributePaths = {"uploader"})
    List<AnalysisGame> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"uploader"})
    List<AnalysisGame> findByStatusOrderByCreatedAtDesc(String status);

    @EntityGraph(attributePaths = {"uploader"})
    @Query("""
        select g
        from AnalysisGame g
        left join g.uploader u
        where (:status is null or g.status = :status)
          and (
            :keyword is null
            or lower(coalesce(u.nickname, '')) like concat('%', :keyword, '%')
            or lower(coalesce(u.email, '')) like concat('%', :keyword, '%')
            or (:gameId is not null and g.id = :gameId)
          )
        order by g.createdAt desc, g.id desc
        """)
    List<AnalysisGame> findAdminRecords(
        @Param("status") String status,
        @Param("keyword") String keyword,
        @Param("gameId") Long gameId,
        Pageable pageable
    );

    long countByUploaderId(Long uploaderId);

    @EntityGraph(attributePaths = {"uploader"})
    List<AnalysisGame> findTop5ByUploaderIdOrderByCreatedAtDesc(Long uploaderId);

    @EntityGraph(attributePaths = {"uploader"})
    Optional<AnalysisGame> findByIdAndUploaderId(Long id, Long uploaderId);

    Optional<AnalysisGame> findByIdAndStatus(Long id, String status);
}
