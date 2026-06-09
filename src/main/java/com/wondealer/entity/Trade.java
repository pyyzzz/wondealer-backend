package com.wondealer.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "TRADE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trade_id")
    private Long id;

    // 하나의 상품(Item)은 하나의 최종 거래(Trade)를 가짐 (1:1 매핑)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    // 💡 취준 면접 단골 질문 팁: Member 엔티티 하나를 가지고 '판매자'와 '구매자'로 다대일(N:1) 연관관계를 두 번 맺어줘야 해!
    // JPA에게 각각 어떤 컬럼명(FK)으로 매핑될지 @JoinColumn으로 명확히 알려주는 게 핵심이야.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private Member seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private Member buyer;

    @Column(name = "trade_price", nullable = false)
    private Long tradePrice; // 최종 거래 금액 (경매면 최종 낙찰가)

    @Column(name = "trade_type", nullable = false, length = 255)
    private String tradeType; // DIRECT, AUCTION

    // 💡 에스크로 상태 관리를 위한 필드 (예: PAY_WAITING, PAID, SHIPPED, COMPLETED, CANCELED)
    @Column(name = "status", nullable = false, length = 255)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Builder
    public Trade(Item item, Member seller, Member buyer, Long tradePrice, String tradeType) {
        this.item = item;
        this.seller = seller;
        this.buyer = buyer;
        this.tradePrice = tradePrice;
        this.tradeType = tradeType;
        this.status = "PAY_WAITING"; // 거래가 처음 개설되면 대개 '결제 대기' 상태로 시작해!
    }

    // === 💡 변후민 전담: 에스크로 상태 제어 비즈니스 메서드 ===
    // 도메인 주도 설계(DDD) 형태로, 비즈니스 흐름에 따라 상태를 안전하게 변경하는 메서드들이야.

    /**
     * 구매자가 결제를 완료했을 때 (대기 -> 결제완료)
     */
    public void completePayment() {
        if (!this.status.equals("PAY_WAITING")) {
            throw new IllegalStateException("결제 대기 상태의 거래만 결제가 가능합니다.");
        }
        this.status = "PAID";
    }

    /**
     * 판매자가 게임 아이템/물품을 구매자에게 넘겨주었을 때 (결제완료 -> 인계완료)
     */
    public void shipItem() {
        if (!this.status.equals("PAID")) {
            throw new IllegalStateException("결제가 완료된 거래만 물품 인계가 가능합니다.");
        }
        this.status = "SHIPPED";
    }

    /**
     * 구매자가 인계 확인을 눌러 물품 수령을 확정했을 때 (인계완료 -> 거래종료)
     * 이때 비로소 WonPay 지갑 정산 로직이 돌면서 판매자에게 돈이 입금되는 흐름이 정석이야!
     */
    public void completeTrade() {
        if (!this.status.equals("SHIPPED")) {
            throw new IllegalStateException("판매자가 물품을 인계한 상태에서만 거래 확정이 가능합니다.");
        }
        this.status = "COMPLETED";
    }

    /**
     * 거래 취소 (안전결제 대기 중이거나 에스크로 단계에서 분쟁 시 취소)
     */
    public void cancelTrade() {
        this.status = "CANCELED";
    }
}