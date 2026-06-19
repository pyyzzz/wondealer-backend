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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long> {

    // 입찰 처리 시 동시성 제어용 비관적 락 (SELECT ... FOR UPDATE)
    // 반드시 일반 findById() 대신 이 메서드를 사용할 것
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Auction a WHERE a.id = :id")
    Optional<Auction> findByIdWithPessimisticLock(@Param("id") Long id);

    // 전체 진행 중 경매 목록 (gameId 필터 없음)
    Page<Auction> findByStatusAndItem_StatusAndItem_IsDeletedByAdminFalse(
            String status, ItemStatus itemStatus, Pageable pageable);

    // 특정 게임의 진행 중 경매 목록 (gameId 필터 있음)
    Page<Auction> findByStatusAndItem_StatusAndItem_IsDeletedByAdminFalseAndItem_GameCategory_Game_Id(
            String status, ItemStatus itemStatus, Long gameId, Pageable pageable);

    // @Scheduled 배치: endTime이 지난 ONGOING 경매 조회
    @Query("SELECT a FROM Auction a WHERE a.status = 'ONGOING' AND a.endTime < :now")
    List<Auction> findExpiredOngoingAuctions(@Param("now") LocalDateTime now);
}
