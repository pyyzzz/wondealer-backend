package com.wondealer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "game_ranking")
@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor

public class GameRanking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ranking_id")
    private Long id;

    @Column(nullable = false)
    private Integer rank;        // 순위 (1~10)

    @Column(nullable = false, length = 100)
    private String gameName;     // 게임명

    @Column(length = 500)
    private String gameImg;      // 게임메카 이미지 URL
}
