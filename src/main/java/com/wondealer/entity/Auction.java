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
    @JoinColumn(name = "item_id", nullable = false, unique = true)
    private Item item;

    @Column(name = "start_price", nullable = false)
    private Long startPrice;

    @Column(name = "current_price", nullable = false)
    private Long currentPrice;

    @Column(name = "instant_buy_price")
    private Long instantBuyPrice;

    @Column(name = "bid_count", nullable = false)
    private Integer bidCount;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "status", nullable = false, length = 10)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id")
    private Member winner;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 💡 오류 조치 완료: 설계서 스펙에 존재하지 않던 updated_at 필드 완전 영구 삭제 처리

    @OneToMany(mappedBy = "auction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Bid> bids = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // 💡 오류 조치 완료: updated_at 제거에 따라 불필요해진 @PreUpdate 메서드 걷어냄

    @Builder
    public Auction(Item item, Long startPrice, Long instantBuyPrice, LocalDateTime endTime) {
        this.item = item;
        this.startPrice = startPrice;
        this.currentPrice = startPrice;
        this.instantBuyPrice = instantBuyPrice;
        this.bidCount = 0;
        this.endTime = endTime;
        this.status = "ONGOING";
    }

    public void updateBid(Long newPrice) {
        this.currentPrice = newPrice;
        this.bidCount++;
    }

    public void endWithWinner(Member winner) {
        this.status = "ENDED";
        this.winner = winner;
    }

    public void cancelAuction() {
        this.status = "CANCELED";
    }
}