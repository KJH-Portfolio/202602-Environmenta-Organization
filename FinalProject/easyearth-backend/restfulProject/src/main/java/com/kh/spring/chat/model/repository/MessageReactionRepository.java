package com.kh.spring.chat.model.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kh.spring.chat.model.vo.MessageReactionEntity;

public interface MessageReactionRepository extends JpaRepository<MessageReactionEntity, Long> {
    // 이미 공감을 눌렀는지 확인 (중복 방지 체크용)
    Optional<MessageReactionEntity> findByChatMessageIdAndMemberId(Long messageId, Long memberId);
    
    // 리액션 삭제
    void deleteByChatMessageIdAndMemberId(Long messageId, Long memberId);
}
