package com.wondealer.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "BID")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Bid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bid_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id", nullable = false)
    private Auction auction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member bidder; // 입찰자

    @Column(name = "bid_price", nullable = false)
    private Long bidPrice;

    @Column(name = "bid_time", nullable = false)
    private LocalDateTime bidTime;

    @Column(name = "status", nullable = false, length = 50)
    private String status; // VALID, OUTBID, WON

    @PrePersist
    protected void onCreate() {
        this.bidTime = LocalDateTime.now();
    }

    @Builder
    public Bid(Auction auction, Member bidder, Long bidPrice) {
        this.auction = auction;
        this.bidder = bidder;
        this.bidPrice = bidPrice;
        this.status = "VALID"; // 첫 입찰 시에는 유효 상태
    }

    // === 비즈니스 메서드 ===
    public void changeStatus(String status) {
        this.status = status;
    }
}