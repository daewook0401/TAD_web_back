package com.tad.www.api.analysis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tad.www.api.analysis.entity.AnalysisGamePlayerStat;

@Repository
public interface AnalysisGamePlayerStatRepository extends JpaRepository<AnalysisGamePlayerStat, Long> {
}
