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
// 무분별한 객체 생성을 막기 위해 기본 생성자는 무조건 PROTECTED로 제한하는 게 JPA 실무 표준이야!
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wallet_id")
    private Long id;

    // 한 명의 회원은 하나의 지갑만 가짐 (1:1 매핑)
    // 외래키(FK)를 쥐고 있는 쪽이 연관관계의 주인이 되므로 @JoinColumn을 써줘.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 💡 중요: 돈 관련 데이터는 소수점 오차나 비즈니스 확장성을 고려해
    // primitive 타입(long)보다는 Long이나 BigDecimal을 사용하는 게 실무에서 안전해.
    @Column(name = "balance", nullable = false)
    private Long balance; // 사용 가능한 실 잔액

    // 💡 취준 꿀팁 필드: 경매 입찰 시 최고 입찰자의 돈을 아예 빼버리면 나중에 환불할 때 꼬여.
    // 그래서 입찰한 금액만큼은 'locked_amount'에 묶어두고 balance에서는 차감해 보여주는 방식을 많이 써!
    @Column(name = "locked_amount", nullable = false)
    private Long lockedAmount; // 경매 등으로 인해 묶인 유예 금액

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
        this.balance = 0L;       // 지갑이 처음 개설될 때는 0원
        this.lockedAmount = 0L;  // 묶인 돈도 0원
    }

    // === 💡 변후민 전담: 핵심 핵심 비즈니스 도메인 메서드 (도메인 주도 설계) ===
    // 엔티티 자체에 데이터를 변경하는 비즈니스 메서드를 두면 무분별한 @Setter를 막을 수 있어 면접에서 칭찬받아.

    /**
     * 충전하기
     */
    public void deposit(Long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("충전 금액은 0원보다 커야 합니다.");
        }
        this.balance += amount;
    }

    /**
     * 일반 결제 및 출금 (잔액 검증 포함)
     */
    public void withdraw(Long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("출금 또는 결제 금액이 올바르지 않습니다.");
        }
        if (this.balance < amount) {
            // 이 부분은 나중에 네가 만들 CustomException으로 던지면 돼!
            throw new IllegalStateException("잔액이 부족합니다.");
        }
        this.balance -= amount;
    }

    /**
     * 경매 입찰 시 금액 예치 (balance에서 lockedAmount로 이동)
     */
    public void lockFunds(Long amount) {
        if (this.balance < amount) {
            throw new IllegalStateException("입찰을 위한 잔액이 부족합니다.");
        }
        this.balance -= amount;
        this.lockedAmount += amount;
    }

    /**
     * 상위 입찰자가 나타나서 기존 입찰자에게 상환/환불 처리할 때
     */
    public void unlockFunds(Long amount) {
        if (this.lockedAmount < amount) {
            throw new IllegalStateException("해제하려는 예치 금액이 묶인 금액보다 큽니다.");
        }
        this.lockedAmount -= amount;
        this.balance += amount;
    }

    /**
     * 경매가 최종 낙찰되어 예치되어 있던 금액이 판매자에게 완전히 빠져나갈 때
     */
    public void confirmPaymentFromLock(Long amount) {
        if (this.lockedAmount < amount) {
            throw new IllegalStateException("정산하려는 금액이 예치금보다 큽니다.");
        }
        this.lockedAmount -= amount;
        // 이 후 판매자 지갑의 deposit(amount)을 호출해 주면 거래 끝!
    }
}