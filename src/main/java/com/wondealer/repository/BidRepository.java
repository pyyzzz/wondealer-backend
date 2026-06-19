package com.wondealer.repository;

import com.wondealer.entity.Bid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {

    // 동시성 처리: 현재 최고 입찰자 조회 (BidService에서 사용)
    Optional<Bid> findTopByAuctionIdAndStatusOrderByBidPriceDesc(
            Long auctionId, String status);

    // 경매 참여자 수 카운트
    @Query("SELECT COUNT(DISTINCT b.bidder.id) FROM Bid b WHERE b.auction.id = :auctionId")
    long countDistinctParticipants(@Param("auctionId") Long auctionId);

    // 마이페이지 내 입찰 내역 페이징 조회
    Page<Bid> findByBidderId(Long bidderId, Pageable pageable);
}
