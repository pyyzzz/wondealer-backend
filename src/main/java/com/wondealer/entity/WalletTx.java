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

    // 어떤 지갑에서 일어난 트랜잭션인지 연결 (지갑 하나는 여러 내역을 가짐 = 1:N)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    // 💡 중요: CHAR나 VARCHAR로 상태를 바로 저장하기보다 자바의 Enum 타입을 사용하는 것이 안전해.
    // 하지만 DB에는 이 Enum의 '문자열 이름(CHARGE, WITHDRAW 등)'이 고스란히 박히도록 @Enumerated(EnumType.STRING)이 필수야!
    // (기본값인 ORDINAL을 쓰면 숫자로 저장돼서 나중에 순서 바뀌면 대참사 난다)
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private WalletTxType type; // CHARGE(충전), PAYMENT(결제), REFUND(환불), WITHDRAW(출금)

    @Column(name = "amount", nullable = false)
    private Long amount; // 거래 금액

    @Column(name = "fee", nullable = false)
    private Long fee; // 플랫폼 수수료 (없으면 0)

    // 어떤 거래(Trade) 때문에 이 돈이 움직였는지 추적하기 위한 연관관계 필드야.
    // 충전이나 출금일 때는 거래 id가 없으므로 nullable = true(허용) 처리해 줘야 해!
    @Column(name = "trade_id", nullable = true)
    private Long tradeId;

    @Column(name = "description", length = 255)
    private String description; // "메이플스토리 아이템 구매", "카카오페이 충전" 등 내용 기록용

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Builder
    public WalletTx(Wallet wallet, WalletTxType type, Long amount, Long fee, Long tradeId, String description) {
        this.wallet = wallet;
        this.type = type;
        this.amount = amount;
        this.fee = fee != null ? fee : 0L; // 수수료가 null로 들어오면 0원으로 방어해 주는 코드
        this.tradeId = tradeId;
        this.description = description;
    }
}