package com.kh.spring.chat.model.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Entity
@Getter
@Builder
@AllArgsConstructor
@Table(name = "MESSAGE_REACTION", uniqueConstraints = {
        @UniqueConstraint(name = "UQ_MEMBER_REACTION", columnNames = {"MESSAGE_ID", "MEMBER_ID"})
})
public class MessageReactionEntity {
	
	protected MessageReactionEntity() {}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "REACTION_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MESSAGE_ID", nullable = false)
    private ChatMessageEntity chatMessage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_ID", nullable = false)
    private MemberEntity member;

    @Column(name = "EMOJI_TYPE", nullable = false, length = 50)
    private String emojiType;

    // 편의 메서드
    public void updateEmoji(String emojiType) {
        this.emojiType = emojiType;
    }
}
