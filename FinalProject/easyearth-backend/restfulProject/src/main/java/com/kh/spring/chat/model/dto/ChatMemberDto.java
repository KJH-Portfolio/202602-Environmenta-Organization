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
public class ChatMemberDto {
    private Long memberId;
    private String name;
    private String loginId;
    private String profileImageUrl;
    private String role; // OWNER, ADMIN, MEMBER
    private LocalDateTime joinedAt;
}
