package com.wondealer.repository;

import com.wondealer.entity.Auction;
import jakarta.persistence.LockModeType;
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
}