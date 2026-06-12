package com.wondealer.repository;

import com.wondealer.entity.OauthAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OauthAccountRepository extends JpaRepository<OauthAccount, Long> {
    // 구글 로그인 시 기존 계정 조회
    Optional<OauthAccount> findByProviderAndProviderId(String provider, String providerId);
}
