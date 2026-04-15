package com.tad.www.api.analysis.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.tad.www.api.analysis.entity.AnalysisGame;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AnalyzeUploadResponse {

    private Long gameNumber;
    private String winner;
    private String bucket;
    private String objectKey;
    private String screenshotUrl;
    private LocalDateTime createdAt;
    private TeamResponse team1;
    private TeamResponse team2;

    public static AnalyzeUploadResponse from(
        AnalysisGame game,
        AnalysisServiceResponse response,
        List<PlayerStatResponse> team1Players,
        List<PlayerStatResponse> team2Players
    ) {
        return AnalyzeUploadResponse.builder()
            .gameNumber(game.getId())
            .winner(game.getWinner())
            .bucket(game.getBucket())
            .objectKey(game.getObjectKey())
            .screenshotUrl(game.getScreenshotUrl())
            .createdAt(game.getCreatedAt())
            .team1(new TeamResponse(team1Players))
            .team2(new TeamResponse(team2Players))
            .build();
    }

    @Getter
    @Builder
    public static class PlayerStatResponse {
        private Long playerId;
        private String name;
        private Integer kills;
        private Integer deaths;
        private Integer assists;
        private Integer cs;
        private Integer gold;
        private Boolean winner;
        private Integer slotNumber;
    }

    public record TeamResponse(List<PlayerStatResponse> players) {
    }
}
