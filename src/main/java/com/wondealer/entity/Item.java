package com.wondealer.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

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
    @JoinColumn(name = "category_id", nullable = false)
    private GameCategory gameCategory;

    // 1. 설계서 반영: 서버가 없는 게임을 위해 nullable = true (기본값)로 변경
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = true)
    private GameServer gameServer;

    // 2. 설계서 반영: VARCHAR(200)으로 길이 변경
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    // 3. 설계서 반영: 컬럼명이 content가 아니라 description임
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "price", nullable = false)
    private Long price;

    // 4. 설계서 반영: trade_type (DIRECT / AUCTION) 필드 누락 수정
    @Column(name = "trade_type", nullable = false, length = 10)
    private String tradeType;

    // 5. 설계서 반영: 초기 상태는 'SELLING'이며 길이는 VARCHAR(20)
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    // 6. 설계서 반영: 관리자 강제 삭제 여부 플래그 추가 (TINYINT(1) 매핑)
    @Column(name = "is_deleted_by_admin", nullable = false)
    private Boolean isDeletedByAdmin;

    // 7. 설계서 반영: 조회수 필드 누락 수정
    @Column(name = "view_count", nullable = false)
    private Integer viewCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 8. 설계서 반영: 수정일(updated_at) 필드 누락 수정
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

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
    public Item(Member seller, GameCategory gameCategory, GameServer gameServer,
                String title, String description, Long price, String tradeType) {
        this.seller = seller;
        this.gameCategory = gameCategory;
        this.gameServer = gameServer;
        this.title = title;
        this.description = description;
        this.price = price;
        this.tradeType = tradeType;      // DIRECT 또는 AUCTION 받아옴
        this.status = "SELLING";          // 설계서 기본값: 'SELLING'
        this.isDeletedByAdmin = false;   // 설계서 기본값: 0 (false)
        this.viewCount = 0;              // 설계서 기본값: 0
    }
}