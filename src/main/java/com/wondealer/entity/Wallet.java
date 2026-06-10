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

    // 설계서 반영: 지갑은 모든 회원의 소유이므로 외래키 명칭을 'member_id'로 정확히 매핑
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member;

    @Column(name = "balance", nullable = false)
    private Long balance; // 현재 총 잔액 (에스크로 금액 포함)

    @Column(name = "locked_amount", nullable = false)
    private Long lockedAmount; // 경매 입찰/거래 진행 등으로 인해 출금 제한된 유예 금액

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
        this.lockedAmount = 0L;
    }

    // === 핵심 비즈니스 도메인 메서드 (설계서 예외 조건 반영) ===

    /**
     * WonPay 충전하기 (토스페이먼츠 웹훅 성공 시 호출)
     */
    public void deposit(Long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("충전 금액은 0원보다 커야 합니다.");
        }
        this.balance += amount;
    }

    /**
     * 일반 결제 및 출금 신청
     * 설계서 반영: 출금 가능 금액은 (현재 총 잔액 - 묶인 금액) 범위 내여야 함
     */
    public void withdraw(Long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("출금 또는 결제 금액이 올바르지 않습니다.");
        }

        // 설계서 핵심 비즈니스 룰 검증 추가: balance - locked_amount 가 실제 출금 가능한 가용 잔액임
        Long availableBalance = this.balance - this.lockedAmount;
        if (availableBalance < amount) {
            throw new IllegalStateException("에스크로 유예 금액을 제외한 출금 가능 잔액이 부족합니다.");
        }

        this.balance -= amount;
    }

    /**
     * 경매 입찰 또는 즉시 구매 시 대금 에스크로 동결 (locked_amount로 이동)
     * 결제 플로우 가동 시 balance 차감이 아닌 lockedAmount 증가 형태로 제어
     */
    public void lockFunds(Long amount) {
        Long availableBalance = this.balance - this.lockedAmount;
        if (availableBalance < amount) {
            throw new IllegalStateException("입찰 및 대금 유예를 위한 가용 잔액이 부족합니다.");
        }
        // 설계서 에스크로 메커니즘: 총액을 깎는 게 아니라 유예 금액 영역을 증가시켜 출금을 막음
        this.lockedAmount += amount;
    }

    /**
     * 상위 입찰자가 발생하거나 거래가 취소되어 동결된 에스크로 대금을 해제(환불)할 때
     */
    public void unlockFunds(Long amount) {
        if (this.lockedAmount < amount) {
            throw new IllegalStateException("해제하려는 예치 금액이 현재 묶인 금액보다 큽니다.");
        }
        this.lockedAmount -= amount;
    }

    /**
     * 거래가 최종 완료(COMPLETED)되어 에스크로 통장에서 구매자의 대금이 완전 출금 처리될 때
     */
    public void confirmPaymentFromLock(Long amount) {
        if (this.lockedAmount < amount) {
            throw new IllegalStateException("정산 확정하려는 금액이 현재 에스크로 유예 금액보다 큽니다.");
        }
        // 총 잔액과 묶인 금액에서 동시에 구매자의 돈을 영구 차감
        this.lockedAmount -= amount;
        this.balance -= amount;
    }
}