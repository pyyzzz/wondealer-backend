package com.wondealer.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
// 💡 테이블명을 완전히 소문자 'payment'로 변경!
@Table(name = "payment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trade_id", nullable = false)
    private Trade trade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(name = "pg_transaction_id", length = 255)
    private String pgTransactionId;

    @Column(name = "status", nullable = false, length = 255)
    private String status;

    @Column(name = "paid_at")
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
        this.status = "READY";
    }

    public void completePayment(String pgTransactionId) {
        this.pgTransactionId = pgTransactionId;
        this.status = "DONE";
        this.paidAt = LocalDateTime.now();
    }

    public void cancelPayment() {
        this.status = "CANCELED";
    }
}