package com.kh.spring.chat.model.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import com.kh.spring.chat.model.vo.ChatRoomUserEntity;

public interface ChatRoomUserRepository extends JpaRepository<ChatRoomUserEntity, Long> {
    // 특정 방에 특정 유저가 참여 중인지 확인
    Optional<ChatRoomUserEntity> findByChatRoomIdAndMemberId(Long chatRoomId, Long memberId);

    // [동시성 제어] 비관적 락으로 방장 퇴장 시 Race Condition 방지
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM ChatRoomUserEntity u WHERE u.chatRoom.id = :chatRoomId AND u.member.id = :memberId")
    Optional<ChatRoomUserEntity> findByChatRoomIdAndMemberIdWithLock(@Param("chatRoomId") Long chatRoomId, @Param("memberId") Long memberId);

    // 나 말고 다른 참여자 찾기 (1:1 채팅방용) - N+1 방지
    @EntityGraph(attributePaths = {"member"})
    Optional<ChatRoomUserEntity> findFirstByChatRoomIdAndMemberIdNot(Long chatRoomId, Long memberId);

    // 방 인원수 확인 (방장 탈퇴 정책용)
    long countByChatRoomId(Long chatRoomId);
    
    // 특정 상태가 아닌 멤버 수 카운트 (PENDING 제외용)
    long countByChatRoomIdAndInvitationStatusNot(Long chatRoomId, String invitationStatus);

    // 방의 모든 참여자 조회 (알림 발송용) - Member Eager Fetch로 LazyInitializationException 방지
    @EntityGraph(attributePaths = {"member"})
    List<ChatRoomUserEntity> findAllByChatRoomId(Long chatRoomId);
}