package com.tad.www.api.analysis.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tad.www.api.analysis.entity.AnalysisGame;

@Repository
public interface AnalysisGameRepository extends JpaRepository<AnalysisGame, Long> {

    @EntityGraph(attributePaths = {"uploader"})
    List<AnalysisGame> findByUploaderIdOrderByCreatedAtDesc(Long uploaderId);

    @EntityGraph(attributePaths = {"uploader"})
    Optional<AnalysisGame> findByIdAndUploaderId(Long id, Long uploaderId);
}
