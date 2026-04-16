package com.tad.www.api.analysis.dto;

import java.time.LocalDateTime;

import com.tad.www.api.analysis.entity.AnalysisGame;
import com.tad.www.api.analysis.entity.AnalysisGamePlayerStat;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AnalysisPlayerRecordResponse {

    private Long gameNumber;
    private String playerName;
    private String result;
    private String teamKey;
    private Integer slotNumber;
    private Integer kills;
    private Integer deaths;
    private Integer assists;
    private Integer cs;
    private Integer gold;
    private String screenshotUrl;
    private LocalDateTime createdAt;
    private LocalDateTime confirmedAt;

    public static AnalysisPlayerRecordResponse from(AnalysisGamePlayerStat stat) {
        AnalysisGame game = stat.getGame();
        String playerName = stat.getPlayer() == null ? stat.getPlayerNameSnapshot() : stat.getPlayer().getPlayerName();

        return AnalysisPlayerRecordResponse.builder()
            .gameNumber(game.getId())
            .playerName(playerName)
            .result(Boolean.TRUE.equals(stat.getIsWinner()) ? "WIN" : "LOSS")
            .teamKey(stat.getTeamKey())
            .slotNumber(stat.getSlotNumber())
            .kills(stat.getKills())
            .deaths(stat.getDeaths())
            .assists(stat.getAssists())
            .cs(stat.getCs())
            .gold(stat.getGold())
            .screenshotUrl(game.getScreenshotUrl())
            .createdAt(game.getCreatedAt())
            .confirmedAt(game.getConfirmedAt())
            .build();
    }
}
