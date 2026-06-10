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
    // 💡 보완 1: DB 표준 관례(소문자+스네이크 케이스)에 맞춰 컬럼명을 'bidder_id'로 명시하는 것이 리눅스 배포 시 가장 안전해!
    @JoinColumn(name = "bidder_id", nullable = false)
    private Member bidder; //입찰자

    @Column(name = "bid_price", nullable = false)
    private Long bidPrice;

    // 💡 보완 2: 데이터베이스 표준 예약어(Time)와 겹쳐서 간혹 SQL 에러가 나는 것을 방지하기 위해 컬럼명을 'bid_at'으로 지정해두면 훨씬 안전해.
    @Column(name = "bid_at", nullable = false)
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