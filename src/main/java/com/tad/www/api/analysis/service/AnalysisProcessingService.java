package com.tad.www.api.analysis.service;

import java.util.Collections;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import com.tad.www.api.analysis.dto.AnalysisPlayerPayload;
import com.tad.www.api.analysis.dto.AnalysisServiceRequest;
import com.tad.www.api.analysis.dto.AnalysisServiceResponse;
import com.tad.www.api.analysis.dto.AnalysisTeamPayload;
import com.tad.www.api.analysis.entity.AnalysisGame;
import com.tad.www.api.analysis.entity.AnalysisGamePlayerStat;
import com.tad.www.api.analysis.repository.AnalysisGamePlayerStatRepository;
import com.tad.www.api.analysis.repository.AnalysisGameRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisProcessingService {

    private static final String STATUS_DRAFT = "DRAFT";
    private static final String STATUS_FAILED = "FAILED";

    private final AnalysisGameRepository analysisGameRepository;
    private final AnalysisGamePlayerStatRepository analysisGamePlayerStatRepository;
    private final RestClient analysisRestClient;

    @Async
    @Transactional
    public void processDraftAsync(Long gameId, String bucket, String objectKey) {
        AnalysisGame game = analysisGameRepository.findById(gameId).orElse(null);
        if (game == null) {
            return;
        }

        try {
            AnalysisServiceResponse response = requestAnalysis(bucket, objectKey);
            String winner = normalizeNullableText(response.getWinner());

            if (!"team1".equals(winner) && !"team2".equals(winner)) {
                throw new IllegalStateException("분석 서버 winner 값이 올바르지 않습니다.");
            }

            game.setWinner(winner);
            game.setStatus(STATUS_DRAFT);

            saveTeamStats(game, "team1", response.getTeam1(), winner);
            saveTeamStats(game, "team2", response.getTeam2(), winner);
        } catch (Exception e) {
            log.error("Failed to process analysis draft for game {}", gameId, e);
            game.setStatus(STATUS_FAILED);
        }
    }

    private AnalysisServiceResponse requestAnalysis(String bucket, String objectKey) {
        AnalysisServiceResponse response = analysisRestClient.post()
            .uri("/analyze")
            .contentType(MediaType.APPLICATION_JSON)
            .body(AnalysisServiceRequest.builder()
                .bucket(bucket)
                .objectKey(objectKey)
                .build())
            .retrieve()
            .body(AnalysisServiceResponse.class);

        if (response == null) {
            throw new IllegalStateException("분석 서버 응답이 비어 있습니다.");
        }

        return response;
    }

    private void saveTeamStats(AnalysisGame game, String teamKey, AnalysisTeamPayload teamPayload, String winner) {
        List<AnalysisPlayerPayload> players = teamPayload == null || teamPayload.getPlayers() == null
            ? Collections.emptyList()
            : teamPayload.getPlayers();

        for (int i = 0; i < players.size(); i++) {
            AnalysisPlayerPayload payload = players.get(i);
            analysisGamePlayerStatRepository.save(AnalysisGamePlayerStat.builder()
                .game(game)
                .player(null)
                .playerNameSnapshot(normalizeNullableText(payload == null ? null : payload.getName()))
                .teamKey(teamKey)
                .slotNumber(i + 1)
                .kills(payload == null ? null : payload.getKills())
                .deaths(payload == null ? null : payload.getDeaths())
                .assists(payload == null ? null : payload.getAssists())
                .cs(payload == null ? null : payload.getCs())
                .gold(payload == null ? null : payload.getGold())
                .isWinner(winner.equals(teamKey))
                .build());
        }
    }

    private String normalizeNullableText(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
