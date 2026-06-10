package com.wondealer.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "trade")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trade_id")
    private Long id;

    // 설계서 반영: 관계 요약 명세(ITEM ──< TRADE)에 맞춰 @ManyToOne 구조로 변경
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    // 설계서 반영: member_id가 아니라 명확하게 'seller_id' 컬럼 명시
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private Member seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private Member buyer;

    @Column(name = "trade_price", nullable = false)
    private Long tradePrice;

    // 허용 값: DIRECT, AUCTION
    @Column(name = "trade_type", nullable = false, length = 10)
    private String tradeType;

    // 설계서 반영: 길이를 스펙 명세인 20으로 맞춤 (허용 값: PENDING_PAYMENT, PAID, COMPLETED, CANCELLED)
    @Column(name = "status", nullable = false, length = 20)
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
        this.status = "PENDING_PAYMENT"; // 설계서 기본값: 'PENDING_PAYMENT'
    }

    // === 비즈니스 도메인 메서드 (설계서 플로우 기반 리팩토링) ===

    // 결제 완료 시 호출 (WonPay 차감 혹은 토스 웹훅 성공 시 마킹)
    public void completePayment() {
        if (!"PENDING_PAYMENT".equals(this.status)) {
            throw new IllegalStateException("결제 대기 상태의 거래만 결제 처리가 가능합니다.");
        }
        this.status = "PAID";
    }

    // 구매자 수령 확인 또는 7일 경과 시 자동 정산 및 완료
    public void completeTrade() {
        if (!"PAID".equals(this.status)) {
            throw new IllegalStateException("결제가 완료되어 에스크로 보관 중인 거래만 완료할 수 있습니다.");
        }
        this.status = "COMPLETED";
    }

    // 분쟁 처리, 한도 초과, 입찰 취소 시 환불 처리를 위한 취소 로직
    public void cancelTrade() {
        this.status = "CANCELED"; // 설계서 약속 규칙: CANCELLED
    }
}