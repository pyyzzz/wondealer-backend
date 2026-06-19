package com.wondealer.repository;

import com.wondealer.entity.Trade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {

    Optional<Trade> findByItemId(Long itemId);

    // 마이페이지 내 거래 내역 페이징 조회
    Page<Trade> findByBuyerId(Long buyerId, Pageable pageable);
    Page<Trade> findBySellerId(Long sellerId, Pageable pageable);
}
