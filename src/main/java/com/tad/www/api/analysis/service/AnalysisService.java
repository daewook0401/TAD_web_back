package com.tad.www.api.analysis.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import com.tad.www.api.analysis.dto.AnalysisPlayerPayload;
import com.tad.www.api.analysis.dto.AnalysisServiceRequest;
import com.tad.www.api.analysis.dto.AnalysisServiceResponse;
import com.tad.www.api.analysis.dto.AnalysisTeamPayload;
import com.tad.www.api.analysis.dto.AnalyzeUploadResponse;
import com.tad.www.api.analysis.entity.AnalysisGame;
import com.tad.www.api.analysis.entity.AnalysisGamePlayerStat;
import com.tad.www.api.analysis.entity.AnalysisPlayer;
import com.tad.www.api.analysis.repository.AnalysisGamePlayerStatRepository;
import com.tad.www.api.analysis.repository.AnalysisGameRepository;
import com.tad.www.api.analysis.repository.AnalysisPlayerRepository;
import com.tad.www.core.config.minio.MinioStorageService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final MinioStorageService minioStorageService;
    private final AnalysisPlayerRepository analysisPlayerRepository;
    private final AnalysisGameRepository analysisGameRepository;
    private final AnalysisGamePlayerStatRepository analysisGamePlayerStatRepository;
    private final RestClient analysisRestClient;

    @Transactional
    public AnalyzeUploadResponse analyzeAndSave(MultipartFile image) {
        validateImage(image);
        MinioStorageService.StoredObject storedImage = minioStorageService.store(image, "analysis/games");
        AnalysisServiceResponse analysisResponse = requestAnalysis(storedImage.bucket(), storedImage.objectKey());
        validateAnalysisResponse(analysisResponse);

        AnalysisGame savedGame = analysisGameRepository.save(AnalysisGame.builder()
            .bucket(storedImage.bucket())
            .objectKey(storedImage.objectKey())
            .screenshotUrl(storedImage.fileUrl())
            .winner(normalizeWinner(analysisResponse.getWinner()))
            .build());

        List<AnalyzeUploadResponse.PlayerStatResponse> team1Players = saveTeamStats(
            savedGame,
            "team1",
            analysisResponse.getTeam1()
        );
        List<AnalyzeUploadResponse.PlayerStatResponse> team2Players = saveTeamStats(
            savedGame,
            "team2",
            analysisResponse.getTeam2()
        );

        return AnalyzeUploadResponse.from(savedGame, analysisResponse, team1Players, team2Players);
    }

    private void validateImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("분석할 이미지 파일이 필요합니다.");
        }

        String contentType = minioStorageService.normalizeContentType(image.getContentType(), image.getOriginalFilename());
        if (!contentType.startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드할 수 있습니다.");
        }
    }

    private AnalysisServiceResponse requestAnalysis(String bucket, String objectKey) {
        try {
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
        } catch (Exception e) {
            throw new IllegalStateException("분석 서버 호출에 실패했습니다.", e);
        }
    }

    private void validateAnalysisResponse(AnalysisServiceResponse response) {
        String winner = normalizeWinner(response.getWinner());
        if (!"team1".equals(winner) && !"team2".equals(winner)) {
            throw new IllegalStateException("분석 서버 winner 값이 올바르지 않습니다.");
        }
    }

    private List<AnalyzeUploadResponse.PlayerStatResponse> saveTeamStats(
        AnalysisGame game,
        String teamKey,
        AnalysisTeamPayload teamPayload
    ) {
        List<AnalysisPlayerPayload> players = teamPayload == null || teamPayload.getPlayers() == null
            ? Collections.emptyList()
            : teamPayload.getPlayers();

        List<AnalyzeUploadResponse.PlayerStatResponse> responses = new ArrayList<>();

        for (int i = 0; i < players.size(); i++) {
            AnalysisPlayerPayload payload = players.get(i);
            String normalizedName = normalizeNullableText(payload == null ? null : payload.getName());
            AnalysisPlayer player = normalizedName == null
                ? null
                : analysisPlayerRepository.findByPlayerName(normalizedName)
                    .orElseGet(() -> analysisPlayerRepository.save(AnalysisPlayer.builder()
                        .playerName(normalizedName)
                        .build()));

            AnalysisGamePlayerStat savedStat = analysisGamePlayerStatRepository.save(AnalysisGamePlayerStat.builder()
                .game(game)
                .player(player)
                .playerNameSnapshot(normalizedName)
                .teamKey(teamKey)
                .slotNumber(i + 1)
                .kills(payload == null ? null : payload.getKills())
                .deaths(payload == null ? null : payload.getDeaths())
                .assists(payload == null ? null : payload.getAssists())
                .cs(payload == null ? null : payload.getCs())
                .gold(payload == null ? null : payload.getGold())
                .isWinner(game.getWinner().equals(teamKey))
                .build());

            responses.add(AnalyzeUploadResponse.PlayerStatResponse.builder()
                .playerId(player == null ? null : player.getId())
                .name(savedStat.getPlayerNameSnapshot())
                .kills(savedStat.getKills())
                .deaths(savedStat.getDeaths())
                .assists(savedStat.getAssists())
                .cs(savedStat.getCs())
                .gold(savedStat.getGold())
                .winner(savedStat.getIsWinner())
                .slotNumber(savedStat.getSlotNumber())
                .build());
        }

        return responses;
    }

    private String normalizeWinner(String winner) {
        return normalizeNullableText(winner);
    }

    private String normalizeNullableText(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
