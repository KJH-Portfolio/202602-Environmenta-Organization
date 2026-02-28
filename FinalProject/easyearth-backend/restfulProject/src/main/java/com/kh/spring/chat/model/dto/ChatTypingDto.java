package com.kh.spring.chat.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatTypingDto {
    private Long chatRoomId;
    private Long senderId;
    private String senderName;  // 입력중인 사용자 이름
    private boolean isTyping; // true: 입력중, false: 멈춤
}
