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

    // 설계서 반영: 원시 타입 boolean 대신 객체 타입 Boolean 사용 및 columnDefinition 지양
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Builder
    public GameServer(Game game, String serverName, boolean isActive) {
        this.game = game;
        this.serverName = serverName;
        this.isActive = isActive;
    }
    // 비즈니스 로직: 관리자의 서버 활성화/비활성화 상태 제어용 메서드
    public void changeActiveStatus(boolean isActive) {
        this.isActive = isActive;
    }
}