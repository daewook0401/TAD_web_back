package com.tad.www.api.analysis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tad.www.api.analysis.entity.AnalysisGame;

@Repository
public interface AnalysisGameRepository extends JpaRepository<AnalysisGame, Long> {
}
