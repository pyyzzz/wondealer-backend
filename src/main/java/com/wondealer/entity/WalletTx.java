package com.wondealer.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallet_tx")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WalletTx {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tx_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    // 설계서 반영: 길이를 스펙 명세인 20으로 최적화 (CHARGE, USE, REFUND, WITHDRAW, SETTLEMENT)
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private WalletTxType type;

    @Column(name = "amount", nullable = false)
    private Long amount; // 양수: 증가 / 음수: 감소

    @Column(name = "fee") // 설계서 스펙 반영: 출금/충전 수수료 금액 (그 외엔 NULL 가능)
    private Long fee;

    // 설계서 반영: 단순 Long tradeId가 아닌, 외래키 연관 관계(FK) 객체 매핑으로 변경 (충전/출금 시엔 NULL 허용)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trade_id", nullable = true)
    private Trade trade;

    // 설계서 반영: 오직 WITHDRAW(출금) 타입에만 사용되는 상태값 컬럼 추가 (PENDING, COMPLETED)
    @Column(name = "status", length = 10)
    private String status;

    @Column(name = "description", length = 200) // 설계서 스펙 반영: 길이를 200으로 수정
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Builder
    public WalletTx(Wallet wallet, WalletTxType type, Long amount, Long fee, Trade trade, String description) {
        this.wallet = wallet;
        this.type = type;
        this.amount = amount;
        this.fee = fee != null ? fee : 0L;
        this.trade = trade;
        this.description = description;

        // 비즈니스 룰 반영: 출금(WITHDRAW) 트랜잭션이 최초 생성될 때는 기본적으로 'PENDING' 상태로 인입
        if (type == WalletTxType.WITHDRAW) {
            this.status = "PENDING";
        } else {
            this.status = null;
        }
    }

    // === 비즈니스 도메인 메서드 ===

    // 관리자가 출금 처리를 완료(계좌 이체 완료)했을 때 상태를 COMPLETED로 변경하는 로직
    public void completeWithdraw() {
        if (this.type != WalletTxType.WITHDRAW) {
            throw new IllegalStateException("출금 타입의 트랜잭션만 상태를 변경할 수 있습니다.");
        }
        this.status = "COMPLETED";
    }
}