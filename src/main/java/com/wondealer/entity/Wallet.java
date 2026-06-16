package com.wondealer.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallet")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wallet_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "balance", nullable = false)
    private Long balance;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Builder
    public Wallet(Member member) {
        this.member = member;
        this.balance = 0L;
    }

    /**
     * 잔액 증가 — 충전 또는 환불 시 사용
     * WalletTxType.CHARGE, REFUND
     */
    public void deposit(Long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("금액은 0원보다 커야 합니다.");
        }
        this.balance += amount;
    }

    /**
     * 잔액 차감 — 입찰, 결제, 출금 시 사용
     * WalletTxType.USE, WITHDRAW
     // 우리 설계  (즉시 차감/환불)
     // 입찰 시: balance 즉시 차감 (withdraw 사용)
     // outbid 시: balance 즉시 환불 (deposit 사용)
     // lockFunds 관련 메서드 불필요
     */
    public void withdraw(Long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("금액은 0원보다 커야 합니다.");
        }
        if (this.balance < amount) {
            throw new IllegalStateException("잔액이 부족합니다.");
        }
        this.balance -= amount;
    }
}