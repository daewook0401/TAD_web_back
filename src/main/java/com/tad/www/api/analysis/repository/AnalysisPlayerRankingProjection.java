package com.tad.www.api.analysis.repository;

import java.math.BigDecimal;

public interface AnalysisPlayerRankingProjection {

    String getPlayerName();

    Long getWins();

    Long getLosses();

    Long getTotalGames();

    Integer getWinRate();

    BigDecimal getAverageKills();

    BigDecimal getAverageDeaths();

    BigDecimal getAverageAssists();

    BigDecimal getAverageCs();

    BigDecimal getAverageGold();
}
