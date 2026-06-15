package com.wondealer.repository;

import com.wondealer.entity.Auction;
import com.wondealer.entity.ItemStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long> {

    // 💡 동시성 제어용 비관적 락 (SELECT ... FOR UPDATE)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Auction a WHERE a.id = :id")
    Optional<Auction> findByIdWithPessimisticLock(@Param("id") Long id);

    // 💡 설계서 스펙 반영: Item -> GameServer -> Game -> gameName 순으로 객체 그래프 탐색
    // 실시간 경매 아이템 화면에서 [게임명]과 [서버명] 조건에 맞는 진행중(ONGOING)인 경매를 마감 임박순으로 정렬
    List<Auction> findByItem_GameServer_Game_GameNameAndItem_GameServer_ServerNameAndStatusOrderByEndTimeAsc(
            String gameName, String serverName, String status
    );

    /*
     * GET /api/auctions 에서 gameId 없이 조회할 때 사용한다.
     * 경매 목록은 진행 중인 경매만 보여주는 화면이므로 DB 조회 단계에서
     * auction.status=ONGOING, item.status=SELLING, item.isDeletedByAdmin=false 조건을 먼저 건다.
     * 그래야 content, totalElements, totalPages가 모두 진행 중 경매 기준으로 정확하게 계산된다.
     */
    Page<Auction> findByStatusAndItem_StatusAndItem_IsDeletedByAdminFalse(
            String status,
            ItemStatus itemStatus,
            Pageable pageable
    );

    /*
     * GET /api/auctions?gameId=... 에서 특정 게임의 진행 중 경매만 조회할 때 사용한다.
     * Auction -> Item -> GameCategory -> Game 순서로 따라가서 game.id 조건을 DB에서 함께 건다.
     */
    Page<Auction> findByStatusAndItem_StatusAndItem_IsDeletedByAdminFalseAndItem_GameCategory_Game_Id(
            String status,
            ItemStatus itemStatus,
            Long gameId,
            Pageable pageable
    );
}
