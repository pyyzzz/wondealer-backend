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
@Table(name = "AUCTION")
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

    @Column(name = "instant_buy_price") // 즉시 낙찰가 (기획에 따라 null 가능)
    private Long instantBuyPrice;

    @Column(name = "bid_count", nullable = false)
    private int bidCount;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "status", nullable = false, length = 50)
    private String status; // 예: PROGRESS, SUCCESS, CANCELED, END_NO_BID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id") // 낙찰 완료 전까지는 null
    private Member winner;

    @Version // 💡 낙관적 락 제어용 버전 필드 (ERD의 version 스펙 반영)
    private int version;

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
        this.currentPrice = startPrice; // 초기 현재가는 시작가와 동일
        this.instantBuyPrice = instantBuyPrice;
        this.bidCount = 0;
        this.endTime = endTime;
        this.status = "PROGRESS";
    }

    // === 경매 비즈니스 도메인 메서드 ===
    public void updateBid(Long newPrice, Member newBidder) {
        this.currentPrice = newPrice;
        this.bidCount++;
        // 낙관적 락(@Version)에 의해 동시 요청 시 자동으로 충전금 변동 정합성이 보호됨
    }

    public void endWithWinner(Member winner) {
        this.status = "SUCCESS";
        this.winner = winner;
    }

    public void cancelAuction() {
        this.status = "CANCELED";
    }
}