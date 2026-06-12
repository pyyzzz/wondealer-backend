package com.wondealer.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "game_server")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GameServer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "server_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Column(name = "server_name", nullable = false, length = 100)
    private String serverName;

    @Column(name = "is_active", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 1")
    private boolean isActive;

    @Builder
    public GameServer(Game game, String serverName, boolean isActive) {
        this.game = game;
        this.serverName = serverName;
        this.isActive = true;
    }
}