package com.wondealer.repository;

import com.wondealer.entity.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {
    // 기본 CRUD(save, findById, delete 등) 자동 제공
}