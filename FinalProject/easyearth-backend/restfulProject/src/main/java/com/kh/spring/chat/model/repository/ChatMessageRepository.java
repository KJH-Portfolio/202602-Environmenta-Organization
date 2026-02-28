package com.kh.spring.chat.model.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kh.spring.chat.model.vo.ChatMessageEntity;

public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {

    // 채팅방의 전체 메시지 수 조회
    long countByChatRoomId(Long chatRoomId);

    // 특정 메시지 ID보다 큰 메시지 수 조회 (안 읽은 메시지 수 계산용)
    long countByChatRoomIdAndIdGreaterThan(Long chatRoomId, Long lastReadMessageId);

    // 2. 무한 스크롤(커서 기반): 특정 메시지(cursorId)보다 이전에 작성된 메시지를 가져옴
    // 첫 로딩 시에는 cursorId가 아주 큰 값(또는 null 처리)이어야 함
    @Query("""
            SELECT m
            FROM ChatMessageEntity m
            WHERE m.chatRoom.id = :chatRoomId
            AND m.id < :cursorId
            ORDER BY m.createdAt DESC
            """)
    Slice<ChatMessageEntity> findByChatRoomIdAndIdLessThan(
            @Param("chatRoomId") Long chatRoomId,
            @Param("cursorId") Long cursorId,
            Pageable pageable);

    // 첫 진입 시 (가장 최신 메시지 N개)
    Slice<ChatMessageEntity> findByChatRoomIdOrderByCreatedAtDesc(
            @Param("chatRoomId") Long chatRoomId,
            Pageable pageable);

    // 3. 메시지 검색 (키워드 포함, 최신순)
    // CLOB 컬럼에 대해 IgnoreCase(UPPER)를 사용하면 오류가 발생할 수 있으므로, LIKE 연산자를 직접 사용합니다.
    // Oracle의 경우 CLOB에 대해 LIKE 연산이 가능합니다.
    @Query("SELECT m FROM ChatMessageEntity m WHERE m.chatRoom.id = :chatRoomId AND m.content LIKE %:keyword% ORDER BY m.createdAt DESC")
    List<ChatMessageEntity> findByChatRoomIdAndContentContainingOrderByCreatedAtDesc(@Param("chatRoomId") Long chatRoomId, @Param("keyword") String keyword);
    
    // 4. 특정 메시지 ID 이하의 모든 메시지 조회 (읽음 처리 갱신용)
    List<ChatMessageEntity> findByChatRoomIdAndIdLessThanEqual(Long chatRoomId, Long messageId);

    // 5. 특정 범위의 메시지 조회 (읽음 업데이트 최적화용: startId < id <= endId)
    List<ChatMessageEntity> findByChatRoomIdAndIdGreaterThanAndIdLessThanEqual(Long chatRoomId, Long startId, Long endId);

    // 6. 가장 최신 메시지 1개 조회 (채팅방 입장 시 읽음 처리용)
    java.util.Optional<ChatMessageEntity> findFirstByChatRoomIdOrderByCreatedAtDesc(Long chatRoomId);
}
