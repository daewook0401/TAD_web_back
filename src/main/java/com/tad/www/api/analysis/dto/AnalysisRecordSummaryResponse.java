package com.tad.www.api.analysis.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.tad.www.api.analysis.entity.AnalysisGame;
import com.tad.www.api.analysis.entity.AnalysisGamePlayerStat;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AnalysisRecordSummaryResponse {

    private Long gameNumber;
    private String status;
    private String winner;
    private String screenshotUrl;
    private LocalDateTime createdAt;
    private LocalDateTime confirmedAt;
    private Integer recognizedPlayers;
    private Boolean reviewRequired;

    public static AnalysisRecordSummaryResponse from(AnalysisGame game, String screenshotUrl, List<AnalysisGamePlayerStat> stats) {
        int recognizedPlayers = (int) stats.stream()
            .map(AnalysisGamePlayerStat::getPlayerNameSnapshot)
            .filter(name -> name != null && !name.isBlank())
            .count();

        return AnalysisRecordSummaryResponse.builder()
            .gameNumber(game.getId())
            .status(game.getStatus())
            .winner(game.getWinner())
            .screenshotUrl(screenshotUrl)
            .createdAt(game.getCreatedAt())
            .confirmedAt(game.getConfirmedAt())
            .recognizedPlayers(recognizedPlayers)
            .reviewRequired("DRAFT".equalsIgnoreCase(game.getStatus()))
            .build();
    }
}
