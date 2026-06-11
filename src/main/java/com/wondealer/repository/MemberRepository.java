package com.wondealer.repository;

import com.wondealer.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    // 로그인 시 identifier(아이디 또는 이메일) 둘 다 조회
    // CustomUserDetailsService에서 호출
    Optional<Member> findByEmailOrUsername(String email, String username);

    Optional<Member> findByEmail(String email);

    Optional<Member> findByUsername(String username);

    // 회원가입 중복 체크
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByNickname(String nickname);

    // 이름과 이메일로 회원 찾기 (아이디 찾기용)
    Optional<Member> findByNameAndEmail(String name, String email);
}
