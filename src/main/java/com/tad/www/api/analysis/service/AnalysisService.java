package com.tad.www.api.analysis.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.IntStream;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.tad.www.api.analysis.dto.AnalysisDraftPlayerUpdateRequest;
import com.tad.www.api.analysis.dto.AnalysisDraftTeamUpdateRequest;
import com.tad.www.api.analysis.dto.AnalysisDraftUpdateRequest;
import com.tad.www.api.analysis.dto.AnalysisAdminRecordResponse;
import com.tad.www.api.analysis.dto.AnalysisPlayerRecordResponse;
import com.tad.www.api.analysis.dto.AnalysisPlayerRankingResponse;
import com.tad.www.api.analysis.dto.AnalysisRecordSummaryResponse;
import com.tad.www.api.analysis.dto.AnalyzeUploadResponse;
import com.tad.www.api.analysis.entity.AnalysisGame;
import com.tad.www.api.analysis.entity.AnalysisGamePlayerStat;
import com.tad.www.api.analysis.entity.AnalysisPlayer;
import com.tad.www.api.analysis.repository.AnalysisGamePlayerStatRepository;
import com.tad.www.api.analysis.repository.AnalysisGameRepository;
import com.tad.www.api.analysis.repository.AnalysisPlayerRepository;
import com.tad.www.api.user.entity.User;
import com.tad.www.core.config.garage.GarageStorageService;
import com.tad.www.core.util.TextUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AnalysisService {

    private static final String STATUS_DRAFT = "DRAFT";
    private static final String STATUS_CONFIRMED = "CONFIRMED";
    private static final String STATUS_PROCESSING = "PROCESSING";
    private static final String STATUS_FAILED = "FAILED";
    private static final long DEFAULT_MIN_GAMES = 1L;
    private static final int DEFAULT_RANKING_LIMIT = 100;
    private static final int MAX_RANKING_LIMIT = 300;
    private static final int DEFAULT_ADMIN_RECORD_LIMIT = 100;
    private static final int MAX_ADMIN_RECORD_LIMIT = 300;

    private final GarageStorageService garageStorageService;
    private final AnalysisPlayerRepository analysisPlayerRepository;
    private final AnalysisGameRepository analysisGameRepository;
    private final AnalysisGamePlayerStatRepository analysisGamePlayerStatRepository;
    private final AnalysisProcessingService analysisProcessingService;

    @Transactional
    public AnalyzeUploadResponse uploadDraft(User currentUser, MultipartFile image) {
        validateImage(image);
        GarageStorageService.StoredObject storedImage = garageStorageService.store(image, "analysis/games");

        AnalysisGame savedGame = analysisGameRepository.save(AnalysisGame.builder()
            .uploader(currentUser)
            .bucket(storedImage.bucket())
            .objectKey(storedImage.objectKey())
            .screenshotUrl(storedImage.fileUrl())
            .winner(null)
            .status(STATUS_PROCESSING)
            .build());

        dispatchDraftProcessing(savedGame.getId(), storedImage.bucket(), storedImage.objectKey());

        return buildDetailResponse(savedGame);
    }

    @Transactional(readOnly = true)
    public List<AnalysisRecordSummaryResponse> getMyRecords(User currentUser) {
        return analysisGameRepository.findByUploaderIdOrderByCreatedAtDesc(currentUser.getId())
            .stream()
            .map(game -> AnalysisRecordSummaryResponse.from(
                game,
                publicScreenshotUrl(game),
                analysisGamePlayerStatRepository.findByGameIdOrderByTeamKeyAscSlotNumberAsc(game.getId())
            ))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<AnalysisAdminRecordResponse> getAdminRecords(String status, String keyword, Integer limit) {
        String normalizedStatus = TextUtils.normalizeNullable(status);
        normalizedStatus = normalizedStatus == null ? null : normalizedStatus.toUpperCase(Locale.ROOT);
        normalizedStatus = isRecordStatus(normalizedStatus) ? normalizedStatus : null;
        String normalizedKeyword = TextUtils.normalizeNullable(keyword);
        normalizedKeyword = normalizedKeyword == null ? null : normalizedKeyword.toLowerCase(Locale.ROOT);
        Long gameId = parseGameId(normalizedKeyword);
        int normalizedLimit = limit == null || limit < 1
            ? DEFAULT_ADMIN_RECORD_LIMIT
            : Math.min(limit, MAX_ADMIN_RECORD_LIMIT);

        List<AnalysisGame> games = analysisGameRepository.findAdminRecords(
            normalizedStatus,
            normalizedKeyword,
            gameId,
            PageRequest.of(0, normalizedLimit)
        );

        return games.stream()
            .map(game -> AnalysisAdminRecordResponse.from(
                game,
                publicScreenshotUrl(game),
                analysisGamePlayerStatRepository.findByGameIdOrderByTeamKeyAscSlotNumberAsc(game.getId())
            ))
            .toList();
    }

    @Transactional(readOnly = true)
    public AnalyzeUploadResponse getMyRecordDetail(Long gameId, User currentUser) {
        AnalysisGame game = getAccessibleGame(gameId, currentUser);
        return buildDetailResponse(game);
    }

    @Transactional(readOnly = true)
    public AnalyzeUploadResponse getConfirmedRecordDetail(Long gameId) {
        AnalysisGame game = analysisGameRepository.findByIdAndStatus(gameId, STATUS_CONFIRMED)
            .orElseThrow(() -> new IllegalArgumentException("확정된 경기 기록을 찾을 수 없습니다."));

        return buildDetailResponse(game);
    }

    @Transactional(readOnly = true)
    public List<AnalysisPlayerRankingResponse> getPlayerRankings(String keyword, Long minGames, Integer limit) {
        String normalizedKeyword = TextUtils.normalizeNullable(keyword);
        String compactKeyword = TextUtils.compactLowerCase(normalizedKeyword);
        long normalizedMinGames = minGames == null || minGames < 1 ? DEFAULT_MIN_GAMES : minGames;
        int normalizedLimit = limit == null || limit < 1
            ? DEFAULT_RANKING_LIMIT
            : Math.min(limit, MAX_RANKING_LIMIT);

        var rankings = analysisGamePlayerStatRepository.findPlayerRankings(
                normalizedKeyword,
                compactKeyword,
                normalizedMinGames,
                normalizedLimit
            );

        return IntStream.range(0, rankings.size())
            .mapToObj(index -> {
                var projection = rankings.get(index);
                return AnalysisPlayerRankingResponse.builder()
                    .rank(index + 1)
                    .playerName(projection.getPlayerName())
                    .wins(projection.getWins())
                    .losses(projection.getLosses())
                    .totalGames(projection.getTotalGames())
                    .winRate(projection.getWinRate())
                    .averageKills(projection.getAverageKills())
                    .averageDeaths(projection.getAverageDeaths())
                    .averageAssists(projection.getAverageAssists())
                    .averageCs(projection.getAverageCs())
                    .averageGold(projection.getAverageGold())
                    .build();
            })
            .toList();
    }

    @Transactional(readOnly = true)
    public List<AnalysisPlayerRecordResponse> getPlayerRecords(String playerName) {
        String normalizedPlayerName = TextUtils.normalizeNullable(playerName);
        if (normalizedPlayerName == null) {
            throw new IllegalArgumentException("playerName은 필수입니다.");
        }

        return analysisGamePlayerStatRepository.findRecordsByPlayerNameAndStatus(
                TextUtils.compactLowerCase(normalizedPlayerName),
                STATUS_CONFIRMED
            )
            .stream()
            .map(stat -> AnalysisPlayerRecordResponse.from(stat, publicScreenshotUrl(stat.getGame())))
            .toList();
    }

    @Transactional
    public AnalyzeUploadResponse updateDraft(Long gameId, User currentUser, AnalysisDraftUpdateRequest request) {
        AnalysisGame game = getAccessibleGame(gameId, currentUser);
        ensureDraft(game);

        String normalizedWinner = TextUtils.normalizeNullable(request.getWinner());
        if (!"team1".equals(normalizedWinner) && !"team2".equals(normalizedWinner)) {
            throw new IllegalArgumentException("winner 값은 team1 또는 team2여야 합니다.");
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
        AnalysisGame game = getAccessibleGame(gameId, currentUser);
        ensureDraft(game);

        List<AnalysisGamePlayerStat> stats = analysisGamePlayerStatRepository.findByGameIdOrderByTeamKeyAscSlotNumberAsc(gameId);
        for (AnalysisGamePlayerStat stat : stats) {
            String normalizedName = TextUtils.normalizeNullable(stat.getPlayerNameSnapshot());
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
            stat.setPlayerNameSnapshot(TextUtils.normalizeNullable(playerRequest.getName()));
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
        return AnalyzeUploadResponse.from(game, publicScreenshotUrl(game), team1Players, team2Players);
    }

    private String publicScreenshotUrl(AnalysisGame game) {
        if (game.getBucket() == null || game.getBucket().isBlank() || game.getObjectKey() == null || game.getObjectKey().isBlank()) {
            return game.getScreenshotUrl();
        }
        return garageStorageService.buildPublicFileUrl(game.getBucket(), game.getObjectKey());
    }

    private void dispatchDraftProcessing(Long gameId, String bucket, String objectKey) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            analysisProcessingService.processDraftAsync(gameId, bucket, objectKey);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                analysisProcessingService.processDraftAsync(gameId, bucket, objectKey);
            }
        });
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

    private AnalysisGame getAccessibleGame(Long gameId, User currentUser) {
        if (hasAdminAuthority()) {
            return analysisGameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("내전 기록을 찾을 수 없습니다."));
        }

        return analysisGameRepository.findByIdAndUploaderId(gameId, currentUser.getId())
            .orElseThrow(() -> new IllegalArgumentException("내전 기록을 찾을 수 없습니다."));
    }

    private boolean hasAdminAuthority() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
            .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }

    private boolean isRecordStatus(String status) {
        return STATUS_PROCESSING.equals(status)
            || STATUS_DRAFT.equals(status)
            || STATUS_CONFIRMED.equals(status)
            || STATUS_FAILED.equals(status);
    }

    private Long parseGameId(String keyword) {
        if (keyword == null) {
            return null;
        }

        String normalized = keyword.startsWith("#") ? keyword.substring(1) : keyword;
        try {
            return Long.parseLong(normalized);
        } catch (NumberFormatException ignored) {
            return null;
        }
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

        String contentType = garageStorageService.normalizeContentType(image.getContentType(), image.getOriginalFilename());
        if (!contentType.startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드할 수 있습니다.");
        }
    }

}
