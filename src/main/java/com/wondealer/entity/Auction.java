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
    @JoinColumn(name = "item_id", nullable = false, unique = true) // 설계서 반영: ITEM과 1:1 관계 (UNIQUE 제약 추가)
    private Item item;

    @Column(name = "start_price", nullable = false)
    private Long startPrice;

    @Column(name = "current_price", nullable = false)
    private Long currentPrice;

    @Column(name = "instant_buy_price")
    private Long instantBuyPrice;

    @Column(name = "bid_count", nullable = false)
    private Integer bidCount; // 일관성을 위해 Integer 권장

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    // 설계서 반영: length를 10으로 변경 (허용 값: ONGOING, ENDED, CANCELLED)
    @Column(name = "status", nullable = false, length = 10)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id")
    private Member winner;

    @Version
    @Column(name = "version", nullable = false) // 설계서 명시: 낙관적 락 버전을 테이블 컬럼과 매핑
    private Integer version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 설계서 반영: 테이블 상세 명세에 존재하는 updated_at 필드 추가
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "auction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Bid> bids = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Builder
    public Auction(Item item, Long startPrice, Long instantBuyPrice, LocalDateTime endTime) {
        this.item = item;
        this.startPrice = startPrice;
        this.currentPrice = startPrice;
        this.instantBuyPrice = instantBuyPrice;
        this.bidCount = 0;
        this.endTime = endTime;
        this.status = "ONGOING"; // 설계서 기본값: 'ONGOING'
    }

    // === 경매 비즈니스 도메인 메서드 (설계서 상태값 반영) ===
    public void updateBid(Long newPrice) {
        this.currentPrice = newPrice;
        this.bidCount++;
    }

    public void endWithWinner(Member winner) {
        this.status = "ENDED"; // 설계서 스펙: ENDED
        this.winner = winner;
    }

    public void cancelAuction() {
        this.status = "CANCELLED"; // 설계서 스펙: CANCELLED
    }
}