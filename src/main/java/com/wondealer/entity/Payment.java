package com.wondealer.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "PAYMENT")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long id;

    // 하나의 거래(Trade)는 하나의 결제 내역(Payment)을 가짐
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trade_id", nullable = false)
    private Trade trade;

    // 결제를 수행한 주체 (회원)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "amount", nullable = false)
    private Long amount; // 실제 결제된 금액

    // 💡 실무 꿀팁 필드: PG사(토스, KG이니시스 등)나 가상계열에서 발급해 주는 고유 거래 식별 번호야.
    // 결제 취소나 대조(정산)할 때 무조건 필요한 핵심 필드라 nullable = true를 주거나 결제 방식에 따라 분기해 관리해.
    @Column(name = "pg_transaction_id", length = 255)
    private String pgTransactionId;

    @Column(name = "status", nullable = false, length = 255)
    private String status; // READY(결제준비), DONE(결제완료), CANCELED(결제취소)

    @Column(name = "paid_at") // 실제 결제가 승인 완료된 시각 (미결제 상태면 null일 수 있음)
    private LocalDateTime paidAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Builder
    public Payment(Trade trade, Member member, Long amount, String pgTransactionId) {
        this.trade = trade;
        this.member = member;
        this.amount = amount;
        this.pgTransactionId = pgTransactionId;
        this.status = "READY"; // 처음 데이터 세팅 단계는 READY
    }

    // === 비즈니스 메서드 ===

    /**
     * PG사 결제 승인 성공 시 호출하여 상태를 DONE으로 바꾸고 승인 시간 기록
     */
    public void completePayment(String pgTransactionId) {
        this.pgTransactionId = pgTransactionId;
        this.status = "DONE";
        this.paidAt = LocalDateTime.now();
    }

    /**
     * 결제 취소 처리
     */
    public void cancelPayment() {
        this.status = "CANCELED";
    }
}