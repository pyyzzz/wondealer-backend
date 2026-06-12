package com.wondealer.repository;

import com.wondealer.entity.ChatRoom;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    // 상품 + 구매자로 채팅방 조회 (중복 생성 방지용)
    Optional<ChatRoom> findByItemIdAndBuyerId(Long itemId, Long buyerId);

    // 내 채팅방 목록 (구매자 또는 판매자로 참여한 채팅방)
    List<ChatRoom> findByBuyerIdOrSellerIdOrderByCreatedAtDesc(Long buyerId, Long sellerId);

    // 특정 상품의 모든 채팅방 (거래 완료 시 비활성화 처리용)
    List<ChatRoom> findByItemId(Long itemId);

    // 만료된 채팅방 조회 (@Scheduled 배치용)
    @Query("SELECT c FROM ChatRoom c WHERE c.expiresAt IS NOT NULL AND c.expiresAt < :now AND c.isActive = true")
    List<ChatRoom> findExpiredRooms(@Param("now") LocalDateTime now);
}
