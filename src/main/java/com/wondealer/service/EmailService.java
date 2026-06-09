package com.wondealer.service;

import com.wondealer.entity.EmailVerify;
import com.wondealer.entity.Member;
import com.wondealer.exception.CustomException;
import com.wondealer.repository.EmailVerifyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private final EmailVerifyRepository emailVerifyRepository;

    public void sendVerificationEmail(Member member) {
        // 1. UUID 토큰 생성
        String token = UUID.randomUUID().toString();

        // 2. EMAIL_VERIFY INSERT
        EmailVerify verify = EmailVerify.createEmailVerify(member, token);
        emailVerifyRepository.save(verify);

        // 3. JavaMailSender 발송
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(member.getEmail()); // Member 객체에서 이메일 추출
        message.setSubject("[원딜러] 회원가입 인증 메일");
        message.setText("아래 링크를 클릭하면 인증이 완료됩니다.\n" +
                "http://localhost:8111/auth/email/verify?token=" + token);
        mailSender.send(message);
    }


}