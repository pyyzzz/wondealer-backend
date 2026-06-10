package com.wondealer.repository;

import com.wondealer.entity.GameRanking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GameRankingRepository extends JpaRepository<GameRanking, Long> {

    // 순위 오름차순 ( 1위 - 10위 )
    List<GameRanking> findAllByOrderByRankAsc();
}
