package com.wondealer.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = true)
    private GameServer gameServer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private GameCategory gameCategory;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description",nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "price", nullable = false)
    private Long price;

    // 오류 수정: ItemStatus ENUM 객체 매핑 및 어노테이션 추가
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ItemStatus status;

    // 오류 수정: TradeType ENUM 객체 매핑 및 어노테이션 추가
    @Enumerated(EnumType.STRING)
    @Column(name = "trade_type", nullable = false, length = 10)
    private TradeType tradeType;

    @Column(name = "view_count", nullable = false)
    private int viewCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted_by_admin", nullable = false,
            columnDefinition = "TINYINT(1) DEFAULT 0")
    private boolean isDeletedByAdmin = false;


    // 이미지 1:N 연관관계
    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemImage> images = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Builder
    public Item(Member seller, GameServer gameServer, GameCategory gameCategory,
                String title, String description, Long price, TradeType tradeType) {
        this.seller = seller;
        this.gameServer = gameServer;
        this.gameCategory = gameCategory;
        this.title = title;
        this.description = description;
        this.price = price;
        this.tradeType = tradeType;
        this.status = ItemStatus.SELLING; // 설계서 스펙 기본값: SELLING
        this.viewCount = 0;
    }

    // === 💡 오류 조치: 유실되었던 핵심 비즈니스 도메인 메서드 전면 복구 ===

    /**
     * 상품 조회수 증가
     */
    public void incrementViewCount() {
        this.viewCount++;
    }

    /**
     * 거래 진행에 따른 상품 상태 변경 (예약중)
     */
    public void reserveItem() {
        if (this.status != ItemStatus.SELLING) {
            throw new IllegalStateException("판매 중인 상품만 예약 상태로 변경할 수 있습니다.");
        }
        this.status = ItemStatus.RESERVED;
    }

    /**
     * 거래 완료에 따른 최종 상태 변경 (완료)
     */
    public void completeItem() {
        this.status = ItemStatus.COMPLETED;
    }

    /**
     * 예약 취소 등에 따른 판매중 상태 원복
     */
    public void cancelReservation() {
        if (this.status != ItemStatus.RESERVED) {
            throw new IllegalStateException("예약 상태의 상품만 판매중으로 되돌릴 수 있습니다.");
        }
        this.status = ItemStatus.SELLING;
    }

    /**
     * 상품 소프트 딜리트(Soft Delete)
     */
    public void deleteByAdmin() {
        this.isDeletedByAdmin = true;
        this.status = ItemStatus.DELETED;
    }

    /**
     * 판매자가 상품 정보를 수정한다. null 또는 빈 문자열은 기존 값을 유지한다.
     */
    public void updateItem(String title, String description, Long price) {
        if (title != null && !title.isBlank()) {
            this.title = title;
        }
        if (description != null && !description.isBlank()) {
            this.description = description;
        }
        if (price != null) {
            this.price = price;
        }
    }

    /**
     * 판매자가 상품을 삭제 상태로 변경한다.
     */
    public void deleteBySeller() {
        this.status = ItemStatus.DELETED;
    }
}
