package com.kh.spring.chat.model.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kh.spring.chat.model.vo.ChatRoomEntity;

public interface ChatRoomRepository extends JpaRepository<ChatRoomEntity, Long> {
	
    // 내가 참여한 방 목록 ('ChatRoomUserEntity'와 조인)
    @Query("SELECT r FROM ChatRoomEntity r JOIN ChatRoomUserEntity u ON r.id = u.chatRoom.id WHERE u.member.id = :memberId ORDER BY r.lastMessageAt DESC")
    List<ChatRoomEntity> findChatRoomsByMemberId(@Param("memberId") Long memberId);

    // 1:1 채팅방 중복 체크 (두 명의 멤버가 모두 포함된 SINGLE 타입의 방이 있는지 확인)
    @Query("SELECT r FROM ChatRoomEntity r " +
           "WHERE r.roomType = 'SINGLE' " +
           //해당 채팅방에 생성자가 member로 포함되어있는지 확인
           "AND r.id IN (SELECT u1.chatRoom.id FROM ChatRoomUserEntity u1 WHERE u1.member.id = :creatorId) " +
           //해당 채팅방에 초대받은 member로 포함되어있는지 확인
           "AND r.id IN (SELECT u2.chatRoom.id FROM ChatRoomUserEntity u2 WHERE u2.member.id = :targetMemberId)")
    Optional<ChatRoomEntity> findExistingSingleRoom(@Param("creatorId") Long creatorId, @Param("targetMemberId") Long targetMemberId);

}
