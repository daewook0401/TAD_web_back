package com.tad.www.api.analysis.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "tb_game_player_stat", schema = "analysis")
public class AnalysisGamePlayerStat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private AnalysisGame game;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    private AnalysisPlayer player;

    @Column(name = "player_name_snapshot", length = 50)
    private String playerNameSnapshot;

    @Column(name = "team_key", nullable = false, length = 10)
    private String teamKey;

    @Column(name = "slot_number", nullable = false)
    private Integer slotNumber;

    @Column
    private Integer kills;

    @Column
    private Integer deaths;

    @Column
    private Integer assists;

    @Column
    private Integer cs;

    @Column
    private Integer gold;

    @Column(name = "is_winner", nullable = false)
    private Boolean isWinner;

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
}
