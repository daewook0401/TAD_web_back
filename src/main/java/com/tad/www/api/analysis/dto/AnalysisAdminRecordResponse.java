package com.tad.www.api.analysis.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.tad.www.api.analysis.entity.AnalysisGame;
import com.tad.www.api.analysis.entity.AnalysisGamePlayerStat;
import com.tad.www.api.user.entity.User;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AnalysisAdminRecordResponse {

    private Long gameNumber;
    private Long uploaderId;
    private String uploaderNickname;
    private String uploaderEmail;
    private String status;
    private String winner;
    private String screenshotUrl;
    private LocalDateTime createdAt;
    private LocalDateTime confirmedAt;
    private Integer recognizedPlayers;
    private Boolean reviewRequired;

    public static AnalysisAdminRecordResponse from(AnalysisGame game, String screenshotUrl, List<AnalysisGamePlayerStat> stats) {
        int recognizedPlayers = (int) stats.stream()
            .map(AnalysisGamePlayerStat::getPlayerNameSnapshot)
            .filter(name -> name != null && !name.isBlank())
            .count();

        User uploader = game.getUploader();
        return AnalysisAdminRecordResponse.builder()
            .gameNumber(game.getId())
            .uploaderId(uploader == null ? null : uploader.getId())
            .uploaderNickname(uploader == null ? null : uploader.getNickname())
            .uploaderEmail(uploader == null ? null : uploader.getEmail())
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
