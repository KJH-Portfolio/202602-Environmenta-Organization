package com.kh.spring.chat.model.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// 클라이언트에게 "어떤 채팅방들이 있는지" 혹은 "이 방의 정보가 무엇인지" 목록/상세 형태로 던져주기 위한 통입니다.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomDto {
    
    // [가장 기본적인 방 정보]
    private Long chatRoomId; // 방 번호
    private Long creatorId; // 제일 처음 이 방을 만든 사람 번호 (방장)
    private Long targetMemberId; // 1:1 방을 만들 때, "내가 누구랑 대화할지" 상대방을 짚어주는 번호
    private String title; // 채팅방 이름 (일반적으로 그룹방에만 쓰임)
    private String roomType; // 방 종류 (SINGLE: 1대1, GROUP: 다대다)
    private String lastMessageContent; // 방 목록에 보일 '마지막 대화 내용'
    private String lastMessageType; // 마지막 대화가 사진인지 글씨인지 구분 (TEXT, IMAGE, FILE...)
    private LocalDateTime lastMessageAt; // 마지막 대화가 오간 시간
    private String roomImage; // 채팅방 썸네일 이미지 주소
    
    // [Frontend 편의를 위한 추가 필드]
    // 굳이 다른 곳에 추가로 요청 안 하고 이 데이터만 받아도 화면에 그릴 수 있도록 백엔드쪽에서 미리 가공해서 보냅니다.
    private int memberCount; // 이 방에 지금 몇명 들어와있는지 (참여자 수)
    private String otherMemberName; // (1:1 방일 경우) 나 말고 내 상대방의 닉네임 (방 제목 대신 상대 이름 띄울 용도)
    private String otherMemberProfile; // (1:1 방일 경우) 상대방의 프로필 사진 경로
    private int unreadCount; // 나 기준으로 이 방에 안 읽은 메시지가 총 몇 개 쌓여있는지 (빨간색 뱃지에 들어갈 숫자)
    private Long myLastReadMessageId; // 내가 이 방에서 마지막으로 어디까지 봤었는지 기록 번호
    
    // [공지사항 관련 정보]
    private String noticeContent; // 톡방 상단에 고정된 공지글 텍스트
    private Long noticeMessageId; // 공지로 지정했던 원래 채팅 메시지의 번호
    private String noticeSenderName; // 공지를 띄운 사람의 닉네임
    private String roomName; // 화면 기획에 따라 title 대신 쓸 수 있는 방이름 변수 (주로 프론트 호환용)
    
    // [참여자 전체 목록]
    // "이 방에 누가누가 있나요?" 버튼 눌렀을 때 띄워줄 멤버들 상세 리스트
    private List<ParticipantInfo> participants;
    
    // 즐겨찾기(상단 고정 핀)를 눌러놓은 방인지 여부
    private boolean isFavorite;
    
    // [초대 관련 상태 정보]
    // 아직 초대에 응하지 않았으면 "PENDING", 내가 들어와있으면 "ACCEPTED", 거부했으면 "REJECTED"
    private String invitationStatus; 
    
    // (그룹 방 처음 팔 때) 같이 방을 만들 멤버들의 ID 번호들을 리스트로 묶어서 보내는 용도입니다.
    private List<Long> invitedMemberIds;
    
    // [참여자 1명의 정보를 담는 내부 클래스]
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParticipantInfo {
        private Long memberId; // 참여자 유저 번호
        private String memberName; // 참여자 닉네임
        private String name; // memberName과 똑같은 데이터 (과거 프론트 코드 명칭과의 호환용)
        private String loginId; // 유저 아이디(ex: user01)
        private String profileImageUrl; // 참여자 프사
        private String role; // 직책 (OWNER 방장, ADMIN 관리자, MEMBER 일반인)
        private LocalDateTime joinedAt; // 이 방에 언제 들어왔는지 시간
    }
}
