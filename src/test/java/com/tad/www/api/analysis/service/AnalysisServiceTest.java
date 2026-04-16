package com.tad.www.api.analysis.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tad.www.api.analysis.dto.AnalysisPlayerRankingResponse;
import com.tad.www.api.analysis.dto.AnalysisPlayerRecordResponse;
import com.tad.www.api.analysis.dto.AnalyzeUploadResponse;
import com.tad.www.api.analysis.entity.AnalysisGame;
import com.tad.www.api.analysis.entity.AnalysisGamePlayerStat;
import com.tad.www.api.analysis.entity.AnalysisPlayer;
import com.tad.www.api.analysis.repository.AnalysisGamePlayerStatRepository;
import com.tad.www.api.analysis.repository.AnalysisGameRepository;
import com.tad.www.api.analysis.repository.AnalysisPlayerRankingProjection;
import com.tad.www.api.analysis.repository.AnalysisPlayerRepository;
import com.tad.www.core.config.minio.MinioStorageService;

@ExtendWith(MockitoExtension.class)
class AnalysisServiceTest {

    @Mock
    private MinioStorageService minioStorageService;

    @Mock
    private AnalysisPlayerRepository analysisPlayerRepository;

    @Mock
    private AnalysisGameRepository analysisGameRepository;

    @Mock
    private AnalysisGamePlayerStatRepository analysisGamePlayerStatRepository;

    @Mock
    private AnalysisProcessingService analysisProcessingService;

    @InjectMocks
    private AnalysisService analysisService;

    @Test
    void getPlayerRankingsAssignsSequentialRanksAndUsesDefaults() {
        when(analysisGamePlayerStatRepository.findPlayerRankings(eq(null), eq(1L), eq(100)))
            .thenReturn(List.of(
                projection("Faker", 10L, 2L, 12L, 83, "8.5", "2.1", "9.4", "221.3", "15444.0"),
                projection("Oner", 8L, 4L, 12L, 67, "4.3", "3.0", "10.0", "180.5", "13200.0")
            ));

        List<AnalysisPlayerRankingResponse> rankings = analysisService.getPlayerRankings("   ", null, null);

        assertThat(rankings).hasSize(2);
        assertThat(rankings.get(0).getRank()).isEqualTo(1);
        assertThat(rankings.get(0).getPlayerName()).isEqualTo("Faker");
        assertThat(rankings.get(1).getRank()).isEqualTo(2);
        assertThat(rankings.get(1).getPlayerName()).isEqualTo("Oner");
    }

    @Test
    void getPlayerRankingsNormalizesMinGamesAndClampsLimit() {
        when(analysisGamePlayerStatRepository.findPlayerRankings(eq("ker"), eq(1L), eq(300)))
            .thenReturn(List.of(projection("Keria", 7L, 2L, 9L, 78, "2.0", "1.8", "13.4", "45.0", "9800.0")));

        List<AnalysisPlayerRankingResponse> rankings = analysisService.getPlayerRankings(" ker ", 0L, 999);

        assertThat(rankings).singleElement().extracting(AnalysisPlayerRankingResponse::getPlayerName).isEqualTo("Keria");
        verify(analysisGamePlayerStatRepository).findPlayerRankings("ker", 1L, 300);
    }

    @Test
    void getPlayerRecordsReturnsConfirmedGameRows() {
        AnalysisGame game = AnalysisGame.builder()
            .id(15L)
            .status("CONFIRMED")
            .screenshotUrl("https://example.com/game.png")
            .createdAt(LocalDateTime.of(2026, 4, 16, 10, 0))
            .confirmedAt(LocalDateTime.of(2026, 4, 16, 10, 5))
            .build();
        AnalysisPlayer player = AnalysisPlayer.builder()
            .id(7L)
            .playerName("Faker")
            .build();
        AnalysisGamePlayerStat stat = AnalysisGamePlayerStat.builder()
            .id(100L)
            .game(game)
            .player(player)
            .teamKey("team1")
            .slotNumber(2)
            .kills(8)
            .deaths(1)
            .assists(11)
            .cs(240)
            .gold(15300)
            .isWinner(true)
            .build();

        when(analysisGamePlayerStatRepository.findConfirmedRecordsByPlayerName("Faker"))
            .thenReturn(List.of(stat));

        List<AnalysisPlayerRecordResponse> records = analysisService.getPlayerRecords(" Faker ");

        assertThat(records).singleElement()
            .satisfies(record -> {
                assertThat(record.getGameNumber()).isEqualTo(15L);
                assertThat(record.getPlayerName()).isEqualTo("Faker");
                assertThat(record.getResult()).isEqualTo("WIN");
                assertThat(record.getKills()).isEqualTo(8);
            });
    }

    @Test
    void getPlayerRecordsRequiresPlayerName() {
        assertThatThrownBy(() -> analysisService.getPlayerRecords(" "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("playerName은 필수입니다.");
    }

    @Test
    void getConfirmedRecordDetailReturnsFullGameDetail() {
        AnalysisGame game = AnalysisGame.builder()
            .id(21L)
            .status("CONFIRMED")
            .winner("team1")
            .bucket("tad")
            .objectKey("analysis/games/game.png")
            .screenshotUrl("https://example.com/game.png")
            .createdAt(LocalDateTime.of(2026, 4, 16, 11, 0))
            .confirmedAt(LocalDateTime.of(2026, 4, 16, 11, 5))
            .build();
        AnalysisGamePlayerStat stat = AnalysisGamePlayerStat.builder()
            .id(200L)
            .game(game)
            .playerNameSnapshot("Faker")
            .teamKey("team1")
            .slotNumber(1)
            .kills(10)
            .deaths(2)
            .assists(7)
            .cs(250)
            .gold(16000)
            .isWinner(true)
            .build();

        when(analysisGameRepository.findByIdAndStatus(21L, "CONFIRMED")).thenReturn(java.util.Optional.of(game));
        when(analysisGamePlayerStatRepository.findByGameIdOrderByTeamKeyAscSlotNumberAsc(21L)).thenReturn(List.of(stat));

        AnalyzeUploadResponse response = analysisService.getConfirmedRecordDetail(21L);

        assertThat(response.getGameNumber()).isEqualTo(21L);
        assertThat(response.getTeam1().players()).singleElement()
            .satisfies(player -> {
                assertThat(player.getName()).isEqualTo("Faker");
                assertThat(player.getKills()).isEqualTo(10);
                assertThat(player.getWinner()).isTrue();
            });
    }

    private AnalysisPlayerRankingProjection projection(
        String playerName,
        Long wins,
        Long losses,
        Long totalGames,
        Integer winRate,
        String averageKills,
        String averageDeaths,
        String averageAssists,
        String averageCs,
        String averageGold
    ) {
        return new AnalysisPlayerRankingProjection() {
            @Override
            public String getPlayerName() {
                return playerName;
            }

            @Override
            public Long getWins() {
                return wins;
            }

            @Override
            public Long getLosses() {
                return losses;
            }

            @Override
            public Long getTotalGames() {
                return totalGames;
            }

            @Override
            public Integer getWinRate() {
                return winRate;
            }

            @Override
            public BigDecimal getAverageKills() {
                return new BigDecimal(averageKills);
            }

            @Override
            public BigDecimal getAverageDeaths() {
                return new BigDecimal(averageDeaths);
            }

            @Override
            public BigDecimal getAverageAssists() {
                return new BigDecimal(averageAssists);
            }

            @Override
            public BigDecimal getAverageCs() {
                return new BigDecimal(averageCs);
            }

            @Override
            public BigDecimal getAverageGold() {
                return new BigDecimal(averageGold);
            }
        };
    }
}
