package com.kh.spring.chat.event;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// 채팅 서비스 로직에서 발생한 이벤트(알람)를 귀 기울여 듣고 있다가 처리해주는 '리스너(수신자)' 클래스입니다.
@Slf4j // 로그 출력을 위해 사용 (log.info 등)
@Component // 스프링이 구동될 때 이 클래스도 자동으로 메모리에 띄워 관리하도록 선언합니다.
@RequiredArgsConstructor // final이 붙은 필드들을 자동으로 주입(생성) 해줍니다.
public class ChatEventListener {

    // 실제로 프론트엔드 화면으로(웹소켓으로) 데이터를 쏴주는 역할을 하는 스프링 내장 도구입니다.
    private final SimpMessagingTemplate messagingTemplate;

    // [매우 중요한 애노테이션 ⭐️]
    // 일반 @EventListener를 쓰지 않고 @TransactionalEventListener를 쓴 이유:
    // 백엔드에서 채팅 데이터를 DB에 '저장(INSERT)' 하고 있는데, 아직 데이터베이스 저장이 완벽하게 안 끝난 상태에서
    // 프론트한테 "새 메시지 왔어!" 라고 먼저 알려버리면? -> 프론트가 DB 털러 왔다가 "어 없는데?" 하고 빈 화면을 보게 됩니다.(동시성 타이밍 불일치)
    // 따라서, phase = TransactionPhase.AFTER_COMMIT 옵션을 걸어서
    // "DB 저장이 완벽하게 완료(Commit)된 이후에만" 이 알림 발송 로직이 실행되도록 순서를 강제하는 것입니다.
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleChatEvent(ChatEvent event) {
        
        // 어떤 경로로 메시지가 날아가는지 서버 로그 창에 찍어줍니다.
        log.info("🔔 [Transaction Committed] Sending WebSocket Event to: {}", event.getDestination());
        
        // 목적지(예: "/topic/user/2")로 내용물(payload)을 포장해서 쏩니다. (프론트엔드로 날아감)
        messagingTemplate.convertAndSend(event.getDestination(), event.getPayload());
    }
}
