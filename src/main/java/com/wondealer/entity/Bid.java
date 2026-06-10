package com.wondealer.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "bid")
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
    @JoinColumn(name = "member_id", nullable = false) // 💡 설계서 스펙 원복완료
    private Member bidder;

    @Column(name = "bid_price", nullable = false)
    private Long bidPrice;

    @Column(name = "bid_time", nullable = false) // 💡 설계서 스펙 원복완료
    private LocalDateTime bidTime;

    @Column(name = "status", nullable = false, length = 10)
    private String status; // 💡 설계서 스펙: VALID / OUTBID / WON

    @PrePersist
    protected void onCreate() {
        this.bidTime = LocalDateTime.now();
    }

    @Builder
    public Bid(Auction auction, Member bidder, Long bidPrice) {
        this.auction = auction;
        this.bidder = bidder;
        this.bidPrice = bidPrice;
        this.status = "VALID";
    }

    public void changeStatus(String status) {
        this.status = status;
    }
}