package com.wondealer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.wondealer.exception.CustomException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
@RequiredArgsConstructor
public class PortOneService {

    private final RestClient.Builder restClientBuilder;

    @Value("${portone.api-base-url:https://api.portone.io}")
    private String apiBaseUrl;

    @Value("${portone.api-secret:}")
    private String apiSecret;

    public PortOnePayment getPayment(String paymentId) {
        if (apiSecret == null || apiSecret.isBlank()) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "포트원 API 시크릿이 설정되어 있지 않습니다.");
        }

        try {
            JsonNode response = restClientBuilder
                    .baseUrl(apiBaseUrl)
                    .build()
                    .get()
                    .uri("/payments/{paymentId}", paymentId)
                    .header("Authorization", "PortOne " + apiSecret)
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null || response.isMissingNode()) {
                throw new CustomException(HttpStatus.BAD_GATEWAY, "포트원 결제 정보를 확인할 수 없습니다.");
            }

            Long totalAmount = extractTotalAmount(response);
            if (totalAmount == null) {
                throw new CustomException(HttpStatus.BAD_GATEWAY, "포트원 결제 금액을 확인할 수 없습니다.");
            }

            return new PortOnePayment(
                    paymentId,
                    response.path("status").asText(),
                    totalAmount
            );
        } catch (RestClientException e) {
            throw new CustomException(HttpStatus.BAD_GATEWAY, "포트원 결제 조회에 실패했습니다.");
        }
    }

    private Long extractTotalAmount(JsonNode response) {
        JsonNode amountTotal = response.path("amount").path("total");
        if (amountTotal.isNumber()) {
            return amountTotal.asLong();
        }

        JsonNode totalAmount = response.path("totalAmount");
        if (totalAmount.isNumber()) {
            return totalAmount.asLong();
        }

        return null;
    }

    @Getter
    @RequiredArgsConstructor
    public static class PortOnePayment {
        private final String paymentId;
        private final String status;
        private final Long totalAmount;
    }
}
