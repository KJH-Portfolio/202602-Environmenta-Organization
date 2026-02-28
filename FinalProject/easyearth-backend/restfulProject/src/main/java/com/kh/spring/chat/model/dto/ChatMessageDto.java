package com.kh.spring.chat.model.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// 이 클래스는 클라이언트(프론트엔드)와 서버(백엔드) 간에 메시지 정보를 주고받기 위한 DTO(데이터 전송 객체)입니다.
// DB 엔티티(ChatMessageEntity)를 그대로 화면에 보내면 내부 구조가 노출되고 복잡하므로, 화면에 필요한 데이터만 골라서 이 상자에 담아 전송합니다.
@Data // Getter, Setter, toString 등을 자동 생성해줍니다.
@Builder // builder 패턴을 사용할 수 있게 합니다.
@NoArgsConstructor // 파라미터가 없는 기본 생성자를 만듭니다.
@AllArgsConstructor // 모든 필드를 파라미터로 받는 생성자를 만듭니다.
public class ChatMessageDto {
    
    // [기본 정보]
    private Long messageId; // 메시지 고유 번호 (DB의 PK)
    private Long chatRoomId; // 이 메시지가 속한 채팅방 번호
    private Long senderId; // 이 메시지를 보낸 사람의 회원 번호
    private String content; // 메시지 실제 내용 (텍스트, 파일 URL 등)
    private String messageType; // 메시지 종류 (TEXT: 일반텍스트, IMAGE: 사진, ENTER: 입장안내, LEAVE: 퇴장안내 등)
    private LocalDateTime createdAt; // 메시지를 보낸 날짜와 시간
    
    // [Frontend 편의를 위한 추가 필드]
    // 클라이언트가 화면을 그릴 때 글쓴이 이름이나 프사를 매번 따로 DB에 물어보지 않도록 DTO에 포함해서 한 번에 내려줍니다.
    private String senderName; // 보낸 사람의 닉네임
    private String senderProfileImage; // 보낸 사람의 프로필 사진 URL
    
    // [답장 관련 정보]
    // 카카오톡의 '답장하기' 기능처럼 특정 메시지를 지정(인용)해서 보낼 때 사용하는 데이터입니다.
    private Long parentMessageId; // 내가 참조(인용)하고 있는 원본 메시지의 번호
    private String parentMessageContent; // 참조하고 있는 원본 메시지의 내용 (화면에 "ㅇㅇ님이 보낸 메시지: 안녕~" 하고 미리보기 띄울 용도)
    private String parentMessageSenderName; // 참조하고 있는 메시지를 작성했던 사람의 이름
    
    // [리액션(공감) 요약 정보]
    // 화면에 '👍 3', '❤️ 1' 처럼 아이콘과 개수를 띄워주기 위한 리스트입니다.
    @Builder.Default
    private List<ReactionSummary> reactions = new ArrayList<>(); // 에러 방지를 위해 기본값 빈 리스트 설정
    
    // [읽음 처리 정보]
    // 카카오톡의 '1' 표시처럼, 아직 이 메시지를 안 읽은 사람의 숫자를 화면에 띄워줍니다.
    private Integer unreadCount; // 안 읽은 사람 수 (보낸 본인은 자기가 읽었으니 제외하고 계산)
    
    // [리액션 정보를 담는 내부 클래스]
    // DTO 안에서만 가볍게 쓸 구조체 용도입니다.
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReactionSummary {
        private String emojiType; // 남긴 이모지 종류 (ex: "LIKE", "HEART")
        private long count; // 해당 이모지를 누른 사람들의 총합
        private boolean selectedByMe; // 내가(현재 조회 요청한 유저가) 이 이모지를 눌렀었는지 여부 (내가 누른 건 색을 칠해주기 위함)
    }
}
