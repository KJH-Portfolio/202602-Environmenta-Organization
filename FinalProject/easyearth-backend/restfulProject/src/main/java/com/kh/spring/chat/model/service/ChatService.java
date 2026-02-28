package com.kh.spring.chat.model.service;

import java.util.List;

import com.kh.spring.chat.model.dto.ChatMessageDto;
import com.kh.spring.chat.model.dto.ChatRoomDto;
import com.kh.spring.chat.model.dto.ChatMemberDto;

// 채팅과 관련된 모든 '핵심 비즈니스 로직(실제 작업)'들의 목록표(메뉴판)입니다.
// 겉으로 보기엔 어떤 일을 하는지만 적혀있고, 실제 작동(구현)은 ChatServiceImpl 클래스가 담당합니다.
public interface ChatService {

    // ==========================================
    // 1. 방 관리기능 (Room Management)
    // ==========================================
    
    // 내(memberId)가 참여하고 있는 채팅방들의 리스트를 가져옵니다. (안 읽은 개수 등 포함)
    List<ChatRoomDto> selectChatRoomList(Long memberId);
    
    // 특정 회원이 기존에 있는 채팅방에 새로 합류합니다. ("~~님이 들어왔습니다" 알림 필요)
    void joinChatRoom(Long roomId, Long memberId);

    // 채팅방에서 스스로 나갑니다. ("~~님이 나갔습니다" 알림 필요)
    void leaveChatRoom(Long roomId, Long memberId);

    // 완전히 새로운 1:1 또는 그룹 채팅방을 만듭니다.
    ChatRoomDto createChatRoom(ChatRoomDto roomDto);

    // 특정 채팅방 한 개의 상세 정보(현재 참여 인원, 공지사항 등)를 가져옵니다.
    ChatRoomDto selectChatRoom(Long roomId, Long memberId);

    // ==========================================
    // 2. 메시지 통신 및 내역 기능 (Message Handling)
    // ==========================================
    
    // 프론트엔드가 보낸 새 메시지를 데이터베이스에 저장하고 결과를 반환합니다.
    ChatMessageDto saveMessage(ChatMessageDto messageDto);

    // 무한 스크롤(페이징) 방식으로 채팅방의 이전 메시지 내역 30개씩을 불러옵니다.
    List<ChatMessageDto> selectMessageList(Long roomId, Long cursorId, Long memberId, int limit);
    
    // 유저가 방을 열었을 때 "어디까지 읽었는지" 카운트를 리셋해줍니다. (빨간색 숫자 없앰)
    void updateReadStatus(Long roomId, Long memberId, Long lastMessageId);
    
    // 메시지 삭제 (Soft Delete): DB에서 안 날리고 "삭제된 메시지입니다"로 텍스트 덮어쓰기
    // requesterId는 방장이 다른 사람의 욕설 등을 지울 때 권한 확인용으로 쓰입니다.
    void softDeleteMessage(Long messageId, Long memberId, Long requesterId);

    // 오래된 대화 중 특정 키워드가 포함된 메시지를 검색합니다.
    List<ChatMessageDto> searchMessages(Long chatRoomId, Long memberId, String keyword, int limit, int offset);
    
    // 새 메시지가 왔을 때, 지금 채팅방 바깥을 보고 있는 유저들에게 "띠링" 푸시 알람을 보냅니다. (비동기 처리)
    void sendGlobalNotifications(ChatMessageDto savedMessage);
    
    // 특정 말풍선에 좋아요(👍), 하트(❤️) 등의 리액션을 달거나 뺍니다.
    void toggleReaction(Long messageId, Long memberId, String emojiType);

    // ==========================================
    // 3. 그룹 관리자 권한 기능 (Role & Kick)
    // ==========================================

    // 방장이 다른 사람에게 방장 권한을 넘길 때 사용합니다.
    void updateRole(Long chatRoomId, Long targetMemberId, Long requesterId, String newRole);

    // 방장 혹은 부방장이 특정 멤버를 강제로 쫓아냅니다. (DB 명부 명단 삭제 및 강제 퇴장 알림)
    void kickMember(Long chatRoomId, Long targetMemberId, Long requesterId);
    
    // 채팅방 상단 고정! 멤버 전체가 보는 공지를 띄우거나, 내립니다.
    void setNotice(Long roomId, Long memberId, Long messageId);
    void clearNotice(Long roomId, Long memberId);
    
    // 이 방을 내 채팅방 목록 맨 위에 고정시켜두는 '즐겨찾기' 스위치
    void toggleFavorite(Long roomId, Long memberId);

    // 내가 만든 방의 이름이나 썸네일 이미지를 변경합니다.
    void updateRoomTitle(Long roomId, Long memberId, String newTitle);
    void updateRoomImage(Long roomId, Long memberId, String imageUrl);

    // ==========================================
    // 4. 초대 및 유저 상태 기능 (Invitation & User)
    // ==========================================
    
    // 어떤 사람을 이 방으로 오라고 초대장을 보냅니다 (초대 상태: PENDING 됨)
    void inviteUser(Long roomId, Long invitedMemberId, Long requesterId);
    
    // 초대받은 사람이 수락(초대장 확인) / 거절(방 들어가기 싫음) 할 때 호출됩니다.
    void acceptInvitation(Long roomId, Long memberId);
    void rejectInvitation(Long roomId, Long memberId);
    
    // 방장이 보낸 초대장을, 유저가 수락하기 전에 뺏들어버립니다(초대 회수).
    void cancelInvitation(Long chatRoomId, Long targetMemberId, Long requesterId);

    // 지금 초대장 보내놓고 답변을 기다리는 중인 사람들 리스트를 조회합니다.
    List<ChatMemberDto> getInvitedUsers(Long chatRoomId);
    
    // 이 방에 지금 합류해있는(수락한) 참전자들 리스트를 모두 가져옵니다.
    List<ChatMemberDto> getChatRoomMembers(Long chatRoomId);
    
    // "초대하기" 버튼을 눌렀을 때, DB 전체에서 닉네임이나 아이디로 회원을 검색해옵니다.
    List<ChatMemberDto> searchMember(String keyword);
    
    // 회원 프사가 바뀌면 채팅 시스템에서도 즉시 바뀐 프사를 반영시킵니다.
    void updateProfile(Long memberId, String profileImageUrl);
}
