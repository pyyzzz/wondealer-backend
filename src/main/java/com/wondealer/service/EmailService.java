package com.wondealer.service;

import com.wondealer.entity.EmailVerify;
import com.wondealer.exception.CustomException;
import com.wondealer.repository.EmailVerifyRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private final EmailVerifyRepository emailVerifyRepository;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    public void sendVerificationEmail(String email) {
        // 1. UUID 토큰 생성
        String token = UUID.randomUUID().toString();

        emailVerifyRepository.save(EmailVerify.createEmailVerify(email, token));

        try {
            MimeMessage message = mailSender.createMimeMessage();
            // true는 멀티파트(첨부파일 등)를 지원하겠다는 의미이며, "UTF-8" 인코딩을 설정합니다.
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("[원딜러] 회원가입 인증 메일");

            // 네이버, 구글 등에서 모두 클릭 가능하도록 <a> 태그를 활용한 HTML 본문을 만듭니다.
            String url = frontendUrl + "/verify-email?token=" + token;
            String htmlContent = "<p>아래 링크를 클릭하면 인증이 완료됩니다.</p>" +
                    "<p><a href='" + url + "' target='_blank' style='color: #0066cc; text-decoration: underline;'>" + url + "</a></p>";

            // 두 번째 인자에 true를 넣어야 HTML 태그가 메일에서 작동합니다.
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MailAuthenticationException e) {
            log.error("Gmail SMTP authentication failed. Check spring.mail.username and app password. from={}", fromEmail, e);
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "메일 계정 인증에 실패했습니다. Gmail 앱 비밀번호를 확인해주세요.");
        } catch (MessagingException | MailException e) {
            log.error("Failed to send verification email. to={}, from={}", email, fromEmail, e);
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "인증 메일 발송 중 오류가 발생했습니다.");
        }
    }

    /**
     * 범용 메일 발송 메서드 - HTML 지원 버전
     * @param to 받는 사람의 이메일 주소
     * @param subject 메일 제목
     * @param text 메일 본문 내용
     */

    public void sendEmail(String to, String subject, String text) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);

            // true 옵션 덕분에 text 안의 HTML 태그가 실제 링크나 버튼으로 변신함!
            helper.setText(text, true);

            mailSender.send(message);
        } catch (MailAuthenticationException e) {
            log.error("Gmail SMTP authentication failed. Check spring.mail.username and app password. from={}", fromEmail, e);
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "메일 계정 인증에 실패했습니다. Gmail 앱 비밀번호를 확인해주세요.");
        } catch (MessagingException | MailException e) {
            log.error("Failed to send email. to={}, from={}, subject={}", to, fromEmail, subject, e);
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "메일 발송 중 오류가 발생했습니다.");
        }
    }


}
