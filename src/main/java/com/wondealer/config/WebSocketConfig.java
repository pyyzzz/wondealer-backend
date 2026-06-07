package com.wondealer.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;


@Configuration
@EnableWebSocketMessageBroker // Raw WebSocket이 아닌 STOMP 메시지 브로커 활성화
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 클라이언트 구독 경로 prefix — 서버가 메시지를 보낼 때 사용
        registry.enableSimpleBroker("/topic");

        // 클라이언트 → 서버 메시지 전송 prefix
        // @MessageMapping("/auction/{id}/bid") 에 매핑됨
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")              // 프론트가 연결할 WebSocket 엔드포인트
                .setAllowedOriginPatterns("*")   // CORS 허용
                .withSockJS();                   // SockJS fallback (브라우저 호환)
    }
}
