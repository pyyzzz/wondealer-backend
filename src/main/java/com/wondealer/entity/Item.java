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
@Table(name = "ITEM")
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
    @JoinColumn(name = "category_id", nullable = false)
    private GameCategory gameCategory; // 대분류 카테고리와 바로 매핑

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = true) // 서버가 없는 게임을 위해 NULL 허용
    private GameServer gameServer;

    @Column(nullable = false, length = 255)
    private String title;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Long price; // 일반거래: 즉시구매가 / 경매: 시작가

    @Column(name = "trade_type", nullable = false, length = 255)
    private String tradeType; // DIRECT, AUCTION

    @Column(nullable = false, length = 255)
    private String status; // SELLING, COMPLETED, DELETED

    @Column(name = "is_deleted_by_admin", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private boolean isDeletedByAdmin;

    @Column(name = "view_count", nullable = false)
    private int viewCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemImage> images = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Builder
    public Item(Member seller, GameCategory gameCategory, GameServer gameServer, String title,
                String description, Long price, String tradeType, String status) {
        this.seller = seller;
        this.gameCategory = gameCategory;
        this.gameServer = gameServer;
        this.title = title;
        this.description = description;
        this.price = price;
        this.tradeType = tradeType;
        this.status = status;
        this.isDeletedByAdmin = false;
        this.viewCount = 0;
    }

    // === 도메인 비즈니스 메서드 ===
    public void updateItem(String title, String description, Long price) {
        if (title != null) this.title = title;
        if (description != null) this.description = description;
        if (price != null) this.price = price;
    }

    public void changeStatus(String status) {
        this.status = status;
    }

    public void incrementViewCount() {
        this.viewCount++;
    }
}