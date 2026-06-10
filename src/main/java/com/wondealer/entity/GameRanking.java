package com.wondealer.entity;

import jakarta.persistence.*;
import lombok.*;

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

    @Column(name = "game_rank", nullable = false)
    private Integer gameRank;    // 순위 (1~10) — rank는 MySQL 예약어라 game_rank 사용

    @Column(nullable = false, length = 100)
    private String gameName;     // 게임명

    @Column(length = 500)
    private String gameImg;      // 게임메카 이미지 URL
}
