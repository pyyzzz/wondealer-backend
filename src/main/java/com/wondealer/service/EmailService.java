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
                "http://localhost:3000/verify-email?token=" + token);
                // 링크주소를 8111 -> 3000으로 변경
        mailSender.send(message);
    }

    /**
     * 범용 메일 발송 메서드
     * @param to 받는 사람의 이메일 주소
     * @param subject 메일 제목
     * @param text 메일 본문 내용
     */

    public void sendEmail(String to, String subject, String text) {
        // 1. 메일 메시지 객체 생성 (Spring에서 제공하는 간단한 메일 메시지 폼)
        SimpleMailMessage message = new SimpleMailMessage();

        // 2. 수신자, 제목, 본문 설정
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        // 3. 메일 발송 (이전에 설정한 SMTP 서버를 통해 전송됨)
        mailSender.send(message);
    }


}