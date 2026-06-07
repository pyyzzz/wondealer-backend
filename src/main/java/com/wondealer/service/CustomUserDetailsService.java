package com.wondealer.service;

import com.wondealer.entity.Member;
import com.wondealer.exception.CustomException;
import com.wondealer.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

/**
 * Spring Security 인증 핵심 서비스
 *
 * 두 가지 시나리오에서 호출됨:
 *
 * 1. 로그인 시 (identifier로 조회)
 *    AuthService.login() → AuthenticationManager.authenticate()
 *    → loadUserByUsername(identifier) → findByEmailOrUsername() → UserDetails 반환
 *
 * 2. Access Token 재발급 시 (memberId로 조회)
 *    AuthService.reissue() → loadUserByUsername(memberId.toString())
 *    → findById() → UserDetails 반환
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        // memberId(숫자)로 재발급 요청인지, identifier(문자열)로 로그인 요청인지 구분
        try {
            Long memberId = Long.parseLong(identifier);
            // 숫자 → 재발급 시나리오: memberId로 직접 조회
            return memberRepository.findById(memberId)
                    .map(this::createUserDetails)
                    .orElseThrow(() -> new UsernameNotFoundException("회원을 찾을 수 없습니다."));
        } catch (NumberFormatException e) {
            // 문자열 → 로그인 시나리오: email 또는 username으로 조회
            return memberRepository.findByEmailOrUsername(identifier, identifier)
                    .map(this::createUserDetails)
                    .orElseThrow(() -> new UsernameNotFoundException("아이디 또는 이메일을 확인해주세요."));
        }
    }

    // Member Entity → Spring Security UserDetails 변환
    private UserDetails createUserDetails(Member member) {
        // 정지된 회원은 로그인 차단
        if (member.isBanned()) {
            throw new CustomException(HttpStatus.FORBIDDEN, "정지된 계정입니다. 고객센터에 문의해주세요.");
        }

        GrantedAuthority authority = new SimpleGrantedAuthority(member.getAuthority().name());

        // subject = memberId → TokenProvider에서 JWT subject로 사용
        return new User(
                String.valueOf(member.getId()),
                member.getPassword(),
                Collections.singleton(authority)
        );
    }
}
