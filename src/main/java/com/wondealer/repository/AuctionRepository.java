package com.wondealer.repository;

import com.wondealer.entity.Auction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long> {

    // 💡 설계서 스펙 반영 고도화:
    // status를 외부에서 "PROGRESS" 등으로 잘못 찌르는 실수를 방지하기 위해
    // JPQL 내부에서 설계서 지정 문자열인 'ONGOING'을 고정 조건으로 박아 넣었습니다.
    @Query("SELECT a FROM Auction a " +
            "JOIN FETCH a.item i " +
            "JOIN i.gameServer gs " +
            "JOIN gs.game g " +
            "WHERE g.gameName = :gameName " +
            "AND gs.serverName = :serverName " +
            "AND a.status = 'ONGOING' " + // 설계서 지정 상태 고정
            "ORDER BY a.endTime ASC")
    List<Auction> findActiveAuctionsByGameAndServer(
            @Param("gameName") String gameName,
            @Param("serverName") String serverName
    );
}