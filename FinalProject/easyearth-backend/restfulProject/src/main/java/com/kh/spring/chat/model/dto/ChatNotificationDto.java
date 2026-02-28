package com.kh.spring.chat.model.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatNotificationDto {
    private Long id; // 알림 고유 ID (선택사항, DB 저장 시 필요)
    private Long targetMemberId; // 수신자 ID
    private String type; // CHAT
    private String content; // "새로운 메시지가 도착했습니다" or 내용 미리보기
    private String url; // 클릭 시 이동할 URL (/chat/room/123)
    private LocalDateTime createdAt;
    
    // 추가 정보
    private Long chatRoomId;
    private String senderName;
    private String senderProfileImage; // 프로필 이미지 추가
    private String messageType; // 메시지 타입 추가 (TEXT, IMAGE, FILE...)
    private String roomName; // 채팅방 이름 추가 (그룹 채팅방 표시용)
}
