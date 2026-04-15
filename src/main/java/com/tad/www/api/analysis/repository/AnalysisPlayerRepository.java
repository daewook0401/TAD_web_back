package com.tad.www.api.analysis.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tad.www.api.analysis.entity.AnalysisPlayer;

@Repository
public interface AnalysisPlayerRepository extends JpaRepository<AnalysisPlayer, Long> {

    Optional<AnalysisPlayer> findByPlayerName(String playerName);
}
