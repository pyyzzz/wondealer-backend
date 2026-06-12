package com.wondealer.repository;

import com.wondealer.entity.WalletTx;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WalletTxRepository extends JpaRepository<WalletTx, Long> {
    // 회원 거래 내역 조회 (최신순)
    List<WalletTx> findByWalletIdOrderByCreatedAtDesc(Long walletId);
}
