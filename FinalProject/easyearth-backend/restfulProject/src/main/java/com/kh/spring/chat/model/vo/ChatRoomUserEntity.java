package com.kh.spring.chat.model.vo;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

// [채팅방]과 [유저]를 연결해주는 중간 다리 역할의 테이블입니다. (N:M 다대다 관계를 1:N - N:1로 풀어줌)
// 즉, "어떤 방"에 "누가" 들어가 있는지를 관리하는 명부(명단) 역할을 합니다.
@Entity
@Getter
@Table(name = "CHAT_ROOM_USER", indexes = {
        // 성능 향상용 색인(Index)입니다. "이 방에 이 유저가 있나?"를 엄청 빠르게 찾기 위해 방 번호와 유저 번호를 묶어 책갈피를 둡니다.
        @Index(name = "IDX_ROOM_MEMBER_COMP", columnList = "CHAT_ROOM_ID, MEMBER_ID")
})
@Builder
@AllArgsConstructor
public class ChatRoomUserEntity {

    // JPA가 요구하는 기본 생성자
    protected ChatRoomUserEntity() {}

    // 이 참여 명부의 고유 번호(PK)입니다.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CHAT_ROOM_USER_ID")
    private Long id;

    // [어느 방인지]에 대한 정보 (지연 로딩: 조인 없이 방 번호만 일단 가지고 있습니다)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CHAT_ROOM_ID", nullable = false)
    private ChatRoomEntity chatRoom;

    // [어떤 유저인지]에 대한 정보 (마찬가지로 지연 로딩)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_ID", nullable = false)
    private MemberEntity member;

    // [안 읽은 메시지 판단 로직 1] 내가 마지막으로 확인한(읽은) 메시지의 고유 ID 번호
    @Column(name = "LAST_READ_MESSAGE_ID")
    private Long lastReadMessageId;

    // [안 읽은 메시지 판단 로직 2] 내가 마지막으로 방을 열어봤을 때, 그 방의 전체 데이터가 총 몇 개(카운트)였는지 기억.
    // ※ 방 전체 카운트 - 내 마지막 카운트 = 내가 아직 안 읽은 개수로 계산합니다.
    @lombok.Builder.Default
    @Column(name = "LAST_READ_MESSAGE_COUNT", columnDefinition = "NUMBER DEFAULT 0")
    private Long lastReadMessageCount = 0L;

    // 내가 이 방에 언제 들어왔는지 입장 날짜 기록
    @CreationTimestamp
    @Column(name = "JOINED_AT")
    private LocalDateTime joinedAt;

    // 방에서의 직책 (예: 방을 처음 만든 사람은 "OWNER", 초대된 일반 회원은 "MEMBER")
    @lombok.Builder.Default
    @Column(name = "ROLE", nullable = false, length = 20)
    private String role = "MEMBER";

    // 즐겨찾기(상단 고정) 설정 여부 (0: 기본, 1: 상단 고정)
    @lombok.Builder.Default
    @Column(name = "IS_FAVORITE", columnDefinition = "NUMBER(1) DEFAULT 0")
    private Integer isFavorite = 0; 

    // 초대 상태 (초대받았으나 고민 중 "PENDING", 수락 후 채팅 켜짐 "ACCEPTED", 거절됨 "REJECTED")
    @lombok.Builder.Default
    @Column(name = "INVITATION_STATUS", length = 20)
    private String invitationStatus = "ACCEPTED"; 

    // 회원이 새 메시지를 확인하면 그 메시지 ID를 기준으로 갱신
    public void updateLastReadMessageId(Long messageId) {
        this.lastReadMessageId = messageId;
    }

    // 역할을 강등/승격시킬 때 갱신 (예: 방장 넘기기)
    public void setRole(String role) {
        this.role = role;
    }

    // 안 읽은 개수를 지우기 위해 마지막으로 본 방의 메시지 총량 갱신
    public void updateLastReadMessageCount(Long count) {
        this.lastReadMessageCount = count;
    }

    // 즐겨찾기를 on/off (토글 스위치) 하는 로직
    public void toggleFavorite() {
        this.isFavorite = (this.isFavorite != null && this.isFavorite == 1) ? 0 : 1;
    }

    // 초대 수락, 거절 상태를 변경
    public void setInvitationStatus(String invitationStatus) {
        this.invitationStatus = invitationStatus;
    }
}
