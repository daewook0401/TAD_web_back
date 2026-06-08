package com.tad.www.api.board.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tad.www.api.board.entity.BoardReport;

@Repository
public interface BoardReportRepository extends JpaRepository<BoardReport, Long> {

    boolean existsByReporter_IdAndTargetTypeAndTargetId(Long reporterId, String targetType, Long targetId);

    @EntityGraph(attributePaths = {"reporter", "reportedUser", "handledBy"})
    List<BoardReport> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"reporter", "reportedUser", "handledBy"})
    List<BoardReport> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);

    long countByStatus(String status);
}
