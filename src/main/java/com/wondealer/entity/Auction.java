package com.wondealer.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "auction")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Auction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auction_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(name = "start_price", nullable = false)
    private Long startPrice;

    @Column(name = "current_price", nullable = false)
    private Long currentPrice;

    @Column(name = "instant_buy_price")
    private Long instantBuyPrice;

    @Column(name = "bid_count", nullable = false)
    private int bidCount;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "status", nullable = false, length = 10)
    private String status; // 💡 설계서 스펙: ONGOING / ENDED / CANCELLED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id")
    private Member winner;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "auction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Bid> bids = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Builder
    public Auction(Item item, Long startPrice, Long instantBuyPrice, LocalDateTime endTime) {
        this.item = item;
        this.startPrice = startPrice;
        this.currentPrice = startPrice;
        this.instantBuyPrice = instantBuyPrice;
        this.bidCount = 0;
        this.endTime = endTime;
        this.status = "ONGOING"; // 💡 설계서 기본값 매칭
    }

    // === 경매 비즈니스 도메인 메서드 ===
    public void updateBid(Long newPrice) {
        this.currentPrice = newPrice;
        this.bidCount++;
    }

    public void endWithWinner(Member winner) {
        this.status = "ENDED"; // 💡 설계서 스펙 반영
        this.winner = winner;
    }

    public void cancelAuction() {
        this.status = "CANCELLED"; // 💡 설계서 스펙 반영
    }
}