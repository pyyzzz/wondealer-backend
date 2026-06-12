package com.wondealer.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "chat_room",
    uniqueConstraints = {
        // 같은 구매자가 같은 상품에 채팅방 중복 생성 방지
        @UniqueConstraint(columnNames = {"buyer_id", "item_id"})
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_room_id")
    private Long id;

    // 구매자 (문의하기 클릭한 사람)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private Member buyer;

    // 판매자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private Member seller;

    // DIRECT 상품만 채팅 가능 — NOT NULL
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    // 결제 완료 후 연결 — 초기에는 NULL
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trade_id")
    private Trade trade;

    // 활성화 여부
    // false: 거래 완료 시 낙찰되지 않은 채팅방 → 메시지 전송 불가, 내역 조회는 가능
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    // 만료 시간 — 거래 완료 시 expiresAt = 지금 + 7일 설정 (생성 시 NULL)
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ChatMessage> messages = new ArrayList<>();

    @PrePersist
    protected void onCreate() { this.createdAt = LocalDateTime.now(); }

    // 거래 완료 시 Trade 연결 + 만료 시간 7일 설정 (낙찰 채팅방)
    public void connectTradeAndSetExpiry(Trade trade) {
        this.trade = trade;
        this.expiresAt = LocalDateTime.now().plusDays(7);
    }

    // 거래 완료 시 낙찰되지 않은 채팅방 비활성화
    public void deactivate() {
        this.isActive = false;
    }
}
