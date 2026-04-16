package com.tad.www.api.analysis.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tad.www.api.analysis.entity.AnalysisGamePlayerStat;

@Repository
public interface AnalysisGamePlayerStatRepository extends JpaRepository<AnalysisGamePlayerStat, Long> {

    List<AnalysisGamePlayerStat> findByGameIdOrderByTeamKeyAscSlotNumberAsc(Long gameId);

    @Query("""
        SELECT s
        FROM AnalysisGamePlayerStat s
        JOIN FETCH s.game g
        LEFT JOIN FETCH s.player p
        WHERE g.status = 'CONFIRMED'
          AND LOWER(COALESCE(p.playerName, s.playerNameSnapshot)) = LOWER(:playerName)
        ORDER BY COALESCE(g.confirmedAt, g.createdAt) DESC, g.id DESC
        """)
    List<AnalysisGamePlayerStat> findConfirmedRecordsByPlayerName(@Param("playerName") String playerName);

    @Query(value = """
        SELECT
            COALESCE(p.player_name, s.player_name_snapshot) AS playerName,
            SUM(CASE WHEN s.is_winner THEN 1 ELSE 0 END) AS wins,
            SUM(CASE WHEN s.is_winner THEN 0 ELSE 1 END) AS losses,
            COUNT(*) AS totalGames,
            CAST(ROUND((SUM(CASE WHEN s.is_winner THEN 1 ELSE 0 END) * 100.0) / NULLIF(COUNT(*), 0)) AS INTEGER) AS winRate,
            ROUND(AVG(s.kills), 1) AS averageKills,
            ROUND(AVG(s.deaths), 1) AS averageDeaths,
            ROUND(AVG(s.assists), 1) AS averageAssists,
            ROUND(AVG(s.cs), 1) AS averageCs,
            ROUND(AVG(s.gold), 1) AS averageGold
        FROM analysis.tb_game_player_stat s
        JOIN analysis.tb_game g ON g.id = s.game_id
        LEFT JOIN analysis.tb_player p ON p.id = s.player_id
        WHERE g.status = 'CONFIRMED'
          AND COALESCE(p.player_name, s.player_name_snapshot) IS NOT NULL
          AND (
                :keyword IS NULL
                OR :keyword = ''
                OR COALESCE(p.player_name, s.player_name_snapshot) ILIKE CONCAT('%', :keyword, '%')
          )
        GROUP BY COALESCE(p.player_name, s.player_name_snapshot)
        HAVING COUNT(*) >= :minGames
        ORDER BY wins DESC, winRate DESC, totalGames DESC, playerName ASC
        LIMIT :limit
        """, nativeQuery = true)
    List<AnalysisPlayerRankingProjection> findPlayerRankings(
        @Param("keyword") String keyword,
        @Param("minGames") long minGames,
        @Param("limit") int limit
    );
}
