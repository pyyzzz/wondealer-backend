package com.wondealer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * AWS S3 클라이언트 빈 등록
 *
 * application.properties의 AWS 설정값 주입
 * 배포 전까지 access-key, secret-key는 비워둬도 서버 실행 가능
 * (상품 이미지 업로드 기능만 동작 안 함)
 *
 * 사용 예:
 *   @Autowired S3Client s3Client;
 *   s3Client.putObject(PutObjectRequest, RequestBody) → S3 업로드
 */
@Configuration
public class S3Config {

    @Value("${spring.cloud.aws.credentials.access-key:}")
    private String accessKey;

    @Value("${spring.cloud.aws.credentials.secret-key:}")
    private String secretKey;

    @Value("${spring.cloud.aws.region.static:ap-northeast-2}")
    private String region;

    @Bean
    public S3Client s3Client() {
        // access-key가 비어있으면 기본 자격증명 체인 사용 (로컬 개발 환경 대응)
        if (accessKey == null || accessKey.isBlank()) {
            return S3Client.builder()
                    .region(Region.of(region))
                    .build();
        }

        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
    }
}
