package com.wondealer.repository;

import com.wondealer.entity.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    Optional<ChatRoom> findByItemIdAndBuyerId(Long itemId, Long buyerId);

    List<ChatRoom> findByBuyerIdOrSellerIdOrderByCreatedAtDesc(Long buyerId, Long sellerId);

    Page<ChatRoom> findByBuyerIdOrSellerId(Long buyerId, Long sellerId, Pageable pageable);

    List<ChatRoom> findByItemId(Long itemId);

    @Query("SELECT c FROM ChatRoom c WHERE c.expiresAt IS NOT NULL AND c.expiresAt < :now AND c.isActive = true")
    List<ChatRoom> findExpiredRooms(@Param("now") LocalDateTime now);
}