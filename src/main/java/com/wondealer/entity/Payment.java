package com.wondealer.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long id;

    // 설계서 반영: TRADE와 1:1 관계의 완벽한 데이터 정합성을 위해 unique = true 추가
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trade_id", nullable = false, unique = true)
    private Trade trade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(name = "pg_transaction_id", length = 255) // 결제 완료 전까지 NULL 허용
    private String pgTransactionId;

    // 설계서 반영: VARCHAR(10)으로 길이 제한 수정 (허용 값: READY, DONE, CANCELLED, FAILED)
    @Column(name = "status", nullable = false, length = 10)
    private String status;

    // 설계서 반영: 누락되었던 결제 수단 컬럼 추가 (허용 값: WONPAY, TOSS)
    @Column(name = "pay_method", nullable = false, length = 10)
    private String payMethod;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Builder
    public Payment(Trade trade, Member member, Long amount, String payMethod) {
        this.trade = trade;
        this.member = member;
        this.amount = amount;
        this.payMethod = (payMethod != null) ? payMethod : "WONPAY"; // 설계서 기본값 'WONPAY' 반영
        this.status = "READY"; // 설계서 기본값 'READY' 반영
        this.pgTransactionId = null; // 초기 생성 시점에는 PG 거래 ID가 없으므로 null 명시
    }

    // === 비즈니스 도메인 메서드 (설계서 스펙 일치화) ===

    // 토스페이먼츠 등 외부 PG사 연동 혹은 원페이 완료 시 호출
    public void completePayment(String pgTransactionId) {
        this.pgTransactionId = pgTransactionId;
        this.status = "DONE";
        this.paidAt = LocalDateTime.now();
    }

    // 결제 취소 및 환불 시 호출 (설계서 ENUM 스펙인 CANCELLED 문자열 적용)
    public void cancelPayment() {
        this.status = "CANCELLED";
    }

    // 결제 실패 시 호출
    public void failPayment() {
        this.status = "FAILED";
    }
}