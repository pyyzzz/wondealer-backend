package com.wondealer.config;

//약관저장
//초기 데이터 주입
//사람(개발자)이 직접 DB에 손대지 않아도, 서버가 스스로 필요한 데이터를 챙겨서 채워 넣는다
//서버가 시작될 때 필요한 모든 "초기 데이터"를 채워 넣는 만능 도구

import com.wondealer.entity.Terms;
import com.wondealer.repository.TermsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final TermsRepository termsRepository;

    @Override
    // String... args : 가변 인자(Variable Arguments)문법, 인자(파라미터)를 0개부터 여러 개까지, 개수 상관없이 받을 수 있게 해주는 문법
    public void run(String... args) throws Exception {
        if (termsRepository.count() == 0) {
            Terms terms1 = Terms.builder()
                    .title("서비스 이용약관")
                    .content("제1조 (목적)")
                    .isRequired(true)
                    .version("v1.0")
                    .build();
            termsRepository.save(terms1);

            Terms terms2 = Terms.builder()
                    .title("개인정보 처리방침")
                    .content("제1조 (개인정보의 처리 목적)")
                    .isRequired(true)
                    .version("v1.0")
                    .build();
            termsRepository.save(terms2);

            System.out.println("데이터 삽입 성공!");
        }
    }
}
