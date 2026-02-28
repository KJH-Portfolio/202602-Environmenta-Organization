package com.kh.spring.chat.model.vo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

// 이 클래스는 누군가가 보낸 메세지 한 개(1건)가 저장되는 [CHAT_MESSAGE] 테이블과 매핑됩니다.
@Entity
@Getter
@Table(name = "CHAT_MESSAGE")
@AllArgsConstructor
@Builder
public class ChatMessageEntity {
    
    // JPA의 필수 요구사항인 기본 생성자를 protected로 만듭니다.
    protected ChatMessageEntity() {}
	
    // 메시지 테이블의 기본키(PK) 입니다. (메시지 고유 번호)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MESSAGE_ID")
    private Long id;

    // 이 메시지가 [어느 방]에 속해있는지 나타내는 다대일(N:1) 관계입니다.
    // FetchType.LAZY: 이 메시지만 조회할 때, 채팅방 정보까지 무조건 DB에서 가져오지 않고 게으르게(나중에 필요할 때) 가져옵니다. (성능 최적화)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CHAT_ROOM_ID", nullable = false) // 방 번호는 빈값이 될 수 없습니다.
    private ChatRoomEntity chatRoom;

    // 이 메시지를 [누가] 보냈는지 나타내는 다대일(N:1) 관계입니다. (여러 메시지를 한 명의 회원이 보냄)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SENDER_ID", nullable = false)
    private MemberEntity sender;

    // 메시지의 실제 내용입니다. 길어질 수 있으므로 @Lob 타입을 적용합니다.
    @Lob
    @Column(name = "CONTENT", nullable = false)
    private String content;

    // 메시지의 종류를 구분합니다. (예: "ENTER", "LEAVE", "TEXT", "FILE", "DELETED" 등)
    @Column(name = "MESSAGE_TYPE", nullable = false, length = 20)
    private String messageType;

    // 이 메시지가 DB에 최초 insert 될 때의 현재 시간을 자동으로 기록합니다. (보낸 시간)
    @CreationTimestamp
    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;
    
    // 답장(스레드) 기능 등에 쓰이는 자기참조입니다. 이 메시지가 어떤 '원래 메시지'에 달린 답장인지 표시합니다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_MESSAGE_ID")
    private ChatMessageEntity parentMessage;
    
    // 이 메시지에 달린 '좋아요', '하트' 등의 리액션(반응) 목록을 양방향으로 가져옵니다.
    @Builder.Default 
    @OneToMany(mappedBy = "chatMessage", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<MessageReactionEntity> reactions = new ArrayList<>();
    // reactions 리스트가 null이 되는 것을 방지하기 위해 빈 ArrayList로 기본값을 지정합니다.
    // mappedBy = "chatMessage" : 리액션 엔티티(MessageReactionEntity) 안에 있는 'chatMessage' 필드가 이 관계의 주인임을 뜻합니다.
    // 이 설정 덕분에 부모인 메시지 객체에서 자식인 자기 리액션들을 쉽게 조회(getReactions())할 수 있죠.
    
    // [Soft Delete 처리용] 클라이언트가 메시지 삭제를 요청했을 때 실제 DB 열을 날려버리는 대신,
    // 내용을 "삭제된 메시지입니다" 로 덮어쓰기 위해 Setter 필드를 열어두었습니다.
    public void setContent(String content) {
        this.content = content;
    }
    
    // [Soft Delete 처리용] "TEXT" 등을 "DELETED" 타입으로 바꾸기 위해 열어둔 Setter입니다.
    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }
}

