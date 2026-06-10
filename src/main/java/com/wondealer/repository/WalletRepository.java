package com.wondealer.repository;

import com.wondealer.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    // 💡 서비스 계층에서 회원의 id(memberId)를 가지고 지갑을 바로 조회할 수 있도록 쿼리 메서드 미리 추가
    Optional<Wallet> findByMemberId(Long memberId);
}