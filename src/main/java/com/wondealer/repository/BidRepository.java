package com.wondealer.repository;

import com.wondealer.entity.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {

    // 💡 팩트체크 완료: 네 프로젝트의 'Bid' 엔티티 구조에 맞춘 중복 제거 참여자 수 카운트
    @Query("SELECT COUNT(DISTINCT b.bidder.id) FROM Bid b WHERE b.auction.id = :auctionId")
    long countDistinctParticipants(@Param("auctionId") Long auctionId);
}