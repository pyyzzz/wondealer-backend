package com.wondealer.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
// 💡 배포 환경(Linux) 에러 방지를 위해 테이블명을 완전히 소문자 'trade'로 변경!
@Table(name = "trade")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trade_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private Member seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private Member buyer;

    // 💡 데이터베이스 표준 관례에 맞춰 모든 컬럼명도 소문자+스네이크 케이스로 유지
    @Column(name = "trade_price", nullable = false)
    private Long tradePrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "trade_type", nullable = false, length = 10)
    private TradeType tradeType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TradeStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Builder
    public Trade(Item item, Member seller, Member buyer, Long tradePrice, TradeType tradeType) {
        this.item = item;
        this.seller = seller;
        this.buyer = buyer;
        this.tradePrice = tradePrice;
        this.tradeType = tradeType;
        this.status = TradeStatus.PAY_WAITING;
    }

    public void completePayment() {
        if (this.status != TradeStatus.PAY_WAITING) {
            throw new IllegalStateException("결제 대기 상태의 거래만 결제가 가능합니다.");
        }
        this.status = TradeStatus.PAID;
    }

    public void shipItem() {
        if (this.status != TradeStatus.PAID) {
            throw new IllegalStateException("결제가 완료된 거래만 물품 인계가 가능합니다.");
        }
        this.status = TradeStatus.SHIPPED;
    }

    public void completeTrade() {
        if (this.status != TradeStatus.SHIPPED) {
            throw new IllegalStateException("판매자가 물품을 인계한 상태에서만 거래 확정이 가능합니다.");
        }
        this.status = TradeStatus.COMPLETED;
    }

    public void cancelTrade() {
        this.status = TradeStatus.CANCELED;
    }
}