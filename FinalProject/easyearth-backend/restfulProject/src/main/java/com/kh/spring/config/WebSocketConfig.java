package com.kh.spring.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

// 이 클래스는 스프링 부트에서 웹소켓(채팅 실시간 통신)을 사용하기 위한 환경을 세팅합니다.
@Configuration // 이 클래스가 스프링의 설정(Config) 파일임을 나타내어, 앱 실행 시 가장 먼저 읽히게 합니다.
@EnableWebSocketMessageBroker // 일반 웹소켓 위에서 메시지를 편하게 주고받기 위해 STOMP라는 프로토콜을 사용하겠다고 선언합니다.
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer { // 웹소켓 메시지 브로커 설정을 커스텀하기 위해 인터페이스를 상속받습니다.

    // 1. 클라이언트(프론트엔드)가 백엔드와 처음으로 웹소켓 선을 연결할 '입구(엔드포인트)'를 지정하는 메서드입니다.
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        
        // 프론트엔드에서 new WebSocket("ws://localhost:8080/ws-chat") 형태로 연결할 주소를 /ws-chat 으로 뚫어줍니다.
        // 시크릿 탭이나 특정 브라우저 환경에서 발생할 수 있는 오류를 피하기 위해 순수 웹소켓 엔드포인트를 하나 엽니다.
        registry.addEndpoint("/ws-chat")
                // CORS 설정: 어떤 도메인에서든 이 소켓에 접속할 수 있도록 허용합니다. (ex: 리액트 3000포트에서도 접속 가능)
                .setAllowedOriginPatterns("*");

        // 만약 순수 웹소켓을 지원하지 않는 구형 브라우저이거나, 네트워크 방화벽이 있는 경우를 대비하는 설정입니다.
        registry.addEndpoint("/ws-chat")
                .setAllowedOriginPatterns("*") // 마찬가지로 모든 도메인 접속을 허용합니다.
                .withSockJS(); // SockJS라는 라이브러리를 통해 웹소켓이 안되면 일반 HTTP로 흉내 내서라도 실시간 통신을 유지하게 합니다.
    }
    
    // 2. 메시지가 어디로 가야 하는지, 길(라우팅)을 정해주는 우체국(브로커) 설정 메서드입니다.
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        
        // [구독(Sub) 주소] 클라이언트가 "나 이 방 메시지 받을래!" 하고 듣는 곳의 접두사를 설정합니다.
        // /topic 은 주로 그룹채팅이나 공지사항(1:N), /queue 는 주로 1:1 메시지(1:1)를 받을 때 사용하도록 규칙을 정한 것입니다.
        registry.enableSimpleBroker("/topic","/queue");

        // [발생(Pub) 주소] 클라이언트가 "나 메시지 보낼게!" 하고 백엔드(서버)로 보낼 때 붙이는 접두사를 설정합니다.
        // 예를 들어 프론트가 /app/chat.send 로 보내면, 백엔드의 @MessageMapping("chat.send") 컨트롤러가 이를 받습니다.
        registry.setApplicationDestinationPrefixes("/app");
    }
}
