package com.kh.spring.chat.model.vo;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

// 이 클래스는 실제 DB의 [CHAT_ROOM] 테이블과 1:1로 매핑되는 객체(엔티티)입니다.
@Entity // JPA가 관리하는 엔티티 클래스임을 선언합니다.
@Getter // 모든 필드의 Getter 메서드를 자동 생성합니다 (ex: getId()).
@Table(name = "CHAT_ROOM") // 이 클래스와 연결될 실제 DB 테이블의 이름을 지정합니다.
@AllArgsConstructor // 모든 필드 값을 파라미터로 받는 생성자를 생성합니다.
@Builder // Builder 패턴을 사용할 수 있게 해줍니다 (ChatRoomEntity.builder().title("방이름").build() 형태).
public class ChatRoomEntity {

    // JPA 엔티티는 기본 생성자가 필수입니다 (프록시 생성 용도). 불필요한 객체 생성을 막기 위해 protected로 설정합니다.
    protected ChatRoomEntity() {}
	
    // 테이블의 기본키(PK - Primary Key)를 설정합니다.
    @Id
    // 데이터베이스의 Identity (Auto-increment) 기능을 사용하여 ID 값을 자동으로 1씩 증가시킵니다.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CHAT_ROOM_ID") // 실제 테이블의 컬럼명과 매핑합니다.
    private Long id; // 채팅방의 고유 ID (방 번호)

    @Column(name = "TITLE", length = 100) // 최대 길이 100의 문자열 컬럼입니다.
    private String title; // 채팅방 이름 (1:1 방은 보통 null, 그룹방은 지정된 이름)

    @Column(name = "ROOM_TYPE", nullable = false, length = 20) // null을 허용하지 않습니다.
    private String roomType; // 방의 타입 (예: "GROUP", "PRIVATE")

    @Column(name = "ROOM_IMAGE")
    private String roomImage; // 채팅방의 썸네일 이미지 URL 혹은 파일명

    @Lob // 대용량 문자열을 저장할 때 사용하는 어노테이션입니다 (DB에 CLOB 타입 등으로 저장됨).
    @Column(name = "LAST_MESSAGE_CONTENT")
    private String lastMessageContent; // 방 목록에서 보일 '가장 마지막으로 보낸 메시지 내용'

    @Column(name = "LAST_MESSAGE_AT")
    private LocalDateTime lastMessageAt; // 마지막 메시지가 전송된 시간 (방 목록 정렬에 사용됨)

    @Column(name = "LAST_MESSAGE_TYPE")
    private String lastMessageType; // 마지막 메시지 종류 (예: "TEXT", "FILE", "SYSTEM")

    @lombok.Builder.Default // Builder 패턴 사용 시 기본값을 0L로 지정합니다.
    @Column(name = "TOTAL_MESSAGE_COUNT", columnDefinition = "NUMBER DEFAULT 0") // DB에서도 기본값을 0으로 줍니다.
    private Long totalMessageCount = 0L; // 이 채팅방에 쌓인 전체 메시지 개수

    @CreationTimestamp // INSERT(최초 저장) 시 현재 시간이 자동으로 들어갑니다.
    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt; // 채팅방 개설 시간
    
    @Lob // 공지사항이 길어질 수 있으므로 Lob 설정
    @Column(name = "NOTICE_CONTENT")
    private String noticeContent; // 상단에 고정된 공지사항 텍스트
    
    @Column(name = "NOTICE_MESSAGE_ID")
    private Long noticeMessageId; // 공지사항으로 등록된 원본 메시지의 고유 ID
    
    @Column(name = "NOTICE_SENDER_ID")
    private Long noticeSenderId; // 공지사항을 등록한 유저의 고유 ID
    
    // 이 방에 속한 메시지들의 목록을 가져오는 양방향 관계 설정입니다.
    // [CASCADE] 이 채팅방 엔티티가 지워질 때(delete), 이 방에 달린 메시지들(ChatMessageEntity)도 DB에서 같이 지워버립니다.
    @lombok.Builder.Default
    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private java.util.List<ChatMessageEntity> messages = new java.util.ArrayList<>();
    
    // [핵심 동시성 제어] 매우 중요한 부분입니다. '낙관적 락(Optimistic Lock)'을 적용합니다.
    // 여러 명이 동시에 메시지를 보낼 때, 누가 먼저 보냈는지 순서를 보장하고 데이터(예: totalMessageCount)의 꼬임을 막기 위한 버전 값입니다.
    @Version
    @Column(name = "VERSION")
    private Long version;
    
    // 새로운 메시지가 왔을 때 채팅방의 '마지막 메시지 장부'를 갱신하는 편의 메서드입니다.
    public void updateLastMessage(String content, LocalDateTime at, String messageType) {
        this.lastMessageContent = content; // 마지막 내용 덮어쓰기
        this.lastMessageAt = at; // 마지막 시간 덮어쓰기
        this.lastMessageType = messageType; // 메시지 종류(텍스트/사진 등) 덮어쓰기
        // 총 메시지 개수를 1개 증가시킵니다. (null이면 0으로 치고 1 증가)
        this.totalMessageCount = (this.totalMessageCount == null ? 0 : this.totalMessageCount) + 1;
    }
    
    // 여기서부터는 필드 값을 수정할 때 캡슐화를 유지하기 위해 열어둔 Setter(수정자)들입니다.
    public void setNoticeContent(String noticeContent) {
        this.noticeContent = noticeContent;
    }
    
    public void setNoticeMessageId(Long noticeMessageId) {
        this.noticeMessageId = noticeMessageId;
    }
    
    public void setNoticeSenderId(Long noticeSenderId) {
        this.noticeSenderId = noticeSenderId;
    }

    public void setTitle(String title) { // 방 제목 수정
        this.title = title;
    }

    public void setRoomImage(String roomImage) { // 방 이미지 수정
        this.roomImage = roomImage;
    }
}