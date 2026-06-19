package com.wondealer.repository;

import com.wondealer.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long>, JpaSpecificationExecutor<Item> {

    // 마이페이지 내 판매 상품 목록 페이징 조회
    Page<Item> findBySellerId(Long memberId, Pageable pageable);
}
