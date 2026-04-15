package com.tad.www.api.analysis.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import com.tad.www.api.analysis.dto.AnalysisDraftPlayerUpdateRequest;
import com.tad.www.api.analysis.dto.AnalysisDraftTeamUpdateRequest;
import com.tad.www.api.analysis.dto.AnalysisDraftUpdateRequest;
import com.tad.www.api.analysis.dto.AnalysisPlayerPayload;
import com.tad.www.api.analysis.dto.AnalysisRecordSummaryResponse;
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
import com.tad.www.api.user.entity.User;
import com.tad.www.core.config.minio.MinioStorageService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AnalysisService {

    private static final String STATUS_DRAFT = "DRAFT";
    private static final String STATUS_CONFIRMED = "CONFIRMED";

    private final MinioStorageService minioStorageService;
    private final AnalysisPlayerRepository analysisPlayerRepository;
    private final AnalysisGameRepository analysisGameRepository;
    private final AnalysisGamePlayerStatRepository analysisGamePlayerStatRepository;
    private final RestClient analysisRestClient;

    @Transactional
    public AnalyzeUploadResponse uploadDraft(User currentUser, MultipartFile image) {
        validateImage(image);
        MinioStorageService.StoredObject storedImage = minioStorageService.store(image, "analysis/games");
        AnalysisServiceResponse analysisResponse = requestAnalysis(storedImage.bucket(), storedImage.objectKey());
        validateAnalysisResponse(analysisResponse);

        AnalysisGame savedGame = analysisGameRepository.save(AnalysisGame.builder()
            .uploader(currentUser)
            .bucket(storedImage.bucket())
            .objectKey(storedImage.objectKey())
            .screenshotUrl(storedImage.fileUrl())
            .winner(normalizeWinner(analysisResponse.getWinner()))
            .status(STATUS_DRAFT)
            .build());

        saveDraftTeamStats(savedGame, "team1", analysisResponse.getTeam1());
        saveDraftTeamStats(savedGame, "team2", analysisResponse.getTeam2());

        return buildDetailResponse(savedGame);
    }

    @Transactional(readOnly = true)
    public List<AnalysisRecordSummaryResponse> getMyRecords(User currentUser) {
        return analysisGameRepository.findByUploaderIdOrderByCreatedAtDesc(currentUser.getId())
            .stream()
            .map(game -> AnalysisRecordSummaryResponse.from(
                game,
                analysisGamePlayerStatRepository.findByGameIdOrderByTeamKeyAscSlotNumberAsc(game.getId())
            ))
            .toList();
    }

    @Transactional(readOnly = true)
    public AnalyzeUploadResponse getMyRecordDetail(Long gameId, User currentUser) {
        AnalysisGame game = getOwnedGame(gameId, currentUser);
        return buildDetailResponse(game);
    }

    @Transactional
    public AnalyzeUploadResponse updateDraft(Long gameId, User currentUser, AnalysisDraftUpdateRequest request) {
        AnalysisGame game = getOwnedGame(gameId, currentUser);
        ensureDraft(game);

        String normalizedWinner = normalizeWinner(request.getWinner());
        if (!"team1".equals(normalizedWinner) && !"team2".equals(normalizedWinner)) {
            throw new IllegalArgumentException("winner 값은 team1 또는 team2 이어야 합니다.");
        }

        game.setWinner(normalizedWinner);

        Map<String, AnalysisGamePlayerStat> statMap = analysisGamePlayerStatRepository.findByGameIdOrderByTeamKeyAscSlotNumberAsc(gameId)
            .stream()
            .collect(LinkedHashMap::new, (map, stat) -> map.put(stat.getTeamKey() + ":" + stat.getSlotNumber(), stat), Map::putAll);

        applyTeamUpdates(statMap, "team1", request.getTeam1(), normalizedWinner);
        applyTeamUpdates(statMap, "team2", request.getTeam2(), normalizedWinner);

        return buildDetailResponse(game);
    }

    @Transactional
    public AnalyzeUploadResponse confirmDraft(Long gameId, User currentUser) {
        AnalysisGame game = getOwnedGame(gameId, currentUser);
        ensureDraft(game);

        List<AnalysisGamePlayerStat> stats = analysisGamePlayerStatRepository.findByGameIdOrderByTeamKeyAscSlotNumberAsc(gameId);
        for (AnalysisGamePlayerStat stat : stats) {
            String normalizedName = normalizeNullableText(stat.getPlayerNameSnapshot());
            stat.setPlayerNameSnapshot(normalizedName);
            stat.setIsWinner(game.getWinner().equals(stat.getTeamKey()));

            if (normalizedName == null) {
                stat.setPlayer(null);
                continue;
            }

            AnalysisPlayer player = analysisPlayerRepository.findByPlayerName(normalizedName)
                .orElseGet(() -> analysisPlayerRepository.save(AnalysisPlayer.builder()
                    .playerName(normalizedName)
                    .build()));
            stat.setPlayer(player);
        }

        game.setStatus(STATUS_CONFIRMED);
        game.setConfirmedAt(LocalDateTime.now());

        return buildDetailResponse(game);
    }

    private void saveDraftTeamStats(AnalysisGame game, String teamKey, AnalysisTeamPayload teamPayload) {
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
                .isWinner(game.getWinner().equals(teamKey))
                .build());
        }
    }

    private void applyTeamUpdates(
        Map<String, AnalysisGamePlayerStat> statMap,
        String teamKey,
        AnalysisDraftTeamUpdateRequest teamRequest,
        String winner
    ) {
        if (teamRequest == null || teamRequest.getPlayers() == null) {
            return;
        }

        for (int index = 0; index < teamRequest.getPlayers().size(); index++) {
            AnalysisDraftPlayerUpdateRequest playerRequest = teamRequest.getPlayers().get(index);
            int slotNumber = playerRequest.getSlotNumber() == null ? index + 1 : playerRequest.getSlotNumber();
            AnalysisGamePlayerStat stat = statMap.get(teamKey + ":" + slotNumber);
            if (stat == null) {
                continue;
            }

            stat.setPlayer(null);
            stat.setPlayerNameSnapshot(normalizeNullableText(playerRequest.getName()));
            stat.setKills(playerRequest.getKills());
            stat.setDeaths(playerRequest.getDeaths());
            stat.setAssists(playerRequest.getAssists());
            stat.setCs(playerRequest.getCs());
            stat.setGold(playerRequest.getGold());
            stat.setIsWinner(winner.equals(teamKey));
        }
    }

    private AnalyzeUploadResponse buildDetailResponse(AnalysisGame game) {
        List<AnalysisGamePlayerStat> stats = analysisGamePlayerStatRepository.findByGameIdOrderByTeamKeyAscSlotNumberAsc(game.getId());
        List<AnalyzeUploadResponse.PlayerStatResponse> team1Players = mapTeamStats(stats, "team1");
        List<AnalyzeUploadResponse.PlayerStatResponse> team2Players = mapTeamStats(stats, "team2");
        return AnalyzeUploadResponse.from(game, team1Players, team2Players);
    }

    private List<AnalyzeUploadResponse.PlayerStatResponse> mapTeamStats(List<AnalysisGamePlayerStat> stats, String teamKey) {
        return stats.stream()
            .filter(stat -> teamKey.equals(stat.getTeamKey()))
            .sorted(Comparator.comparing(AnalysisGamePlayerStat::getSlotNumber))
            .map(stat -> AnalyzeUploadResponse.PlayerStatResponse.builder()
                .statId(stat.getId())
                .playerId(stat.getPlayer() == null ? null : stat.getPlayer().getId())
                .name(stat.getPlayerNameSnapshot())
                .kills(stat.getKills())
                .deaths(stat.getDeaths())
                .assists(stat.getAssists())
                .cs(stat.getCs())
                .gold(stat.getGold())
                .winner(stat.getIsWinner())
                .slotNumber(stat.getSlotNumber())
                .teamKey(stat.getTeamKey())
                .build())
            .toList();
    }

    private AnalysisGame getOwnedGame(Long gameId, User currentUser) {
        return analysisGameRepository.findByIdAndUploaderId(gameId, currentUser.getId())
            .orElseThrow(() -> new IllegalArgumentException("내전 기록을 찾을 수 없습니다."));
    }

    private void ensureDraft(AnalysisGame game) {
        if (!STATUS_DRAFT.equals(game.getStatus())) {
            throw new AccessDeniedException("이미 확정된 내전 기록은 수정할 수 없습니다.");
        }
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
