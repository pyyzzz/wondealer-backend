package com.wondealer.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private Member seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false) // 💡 설계서 매칭: 아이템/게임머니/계정 종류 결정
    private GameCategory gameCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false) // 💡 설계서 매칭: 서버 및 게임 추적의 핵심 다리
    private GameServer gameServer;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "price", nullable = false)
    private Long price;

    @Column(name = "status", nullable = false, length = 20)
    private String status; // ON_SALE, RESERVED, SOLD_OUT

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Builder
    public Item(Member seller, GameCategory gameCategory, GameServer gameServer, String title, String content, Long price) {
        this.seller = seller;
        this.gameCategory = gameCategory;
        this.gameServer = gameServer;
        this.title = title;
        this.content = content;
        this.price = price;
        this.status = "ON_SALE";
    }
}