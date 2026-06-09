package com.wondealer.dto.request;

import com.wondealer.constant.Authority;
import com.wondealer.entity.Member;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SignUpReqDto {

    @NotBlank(message = "아이디를 입력해주세요.")
    @Size(min = 4, max = 20, message = "아이디는 4~20자 사이여야 합니다.")
    private String username;

    @NotBlank(message = "성함을 입력해주세요.")
    private String name;

    @NotBlank(message = "닉네임을 입력해주세요.")
    @Size(min = 2, max = 20, message = "닉네임은 2~20자 사이여야 합니다.")
    private String nickname;

    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
    private String password;

    //약관이 2개라 List, 약관id가 Long이라서
    //약관 선택X =[], 약관 1만 선택=[1], 약관1,2 선택 = [1, 2]
    @NotEmpty(message = "필수 약관에 동의해주세요.")
    private List<Long> termsAgreed;

    // DTO → Entity 변환
    // authority는 클라이언트 입력 불가 — 반드시 ROLE_USER로 고정
    public Member toEntity(PasswordEncoder passwordEncoder) {
        return Member.builder()
                .username(username)
                .name(name)
                .nickname(nickname)
                .email(email)
                .password(passwordEncoder.encode(password))
                .authority(Authority.ROLE_USER) // 보안: 외부에서 ADMIN 설정 불가
                .isEmailVerified(false)
                .isBanned(false)
                .build();
    }
}
