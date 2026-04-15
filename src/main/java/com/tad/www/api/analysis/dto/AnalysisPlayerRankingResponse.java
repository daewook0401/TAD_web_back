package com.tad.www.api.analysis.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AnalysisPlayerRankingResponse {

    private Integer rank;
    private String playerName;
    private Long wins;
    private Long losses;
    private Long totalGames;
    private Integer winRate;
    private BigDecimal averageKills;
    private BigDecimal averageDeaths;
    private BigDecimal averageAssists;
    private BigDecimal averageCs;
    private BigDecimal averageGold;
}
