package com.kh.spring.chat.model.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.*;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.Optional;

import com.kh.spring.chat.model.dto.ChatMessageDto;
import com.kh.spring.chat.model.dto.ChatNotificationDto;
import com.kh.spring.chat.model.dto.ChatRoomDto;
import com.kh.spring.chat.model.dto.ChatMemberDto;
import com.kh.spring.chat.event.ChatEvent;
import com.kh.spring.chat.model.repository.ChatMessageRepository;
import com.kh.spring.chat.model.repository.ChatRoomRepository;
import com.kh.spring.chat.model.repository.ChatRoomUserRepository;
import com.kh.spring.chat.model.repository.MemberRepository;
import com.kh.spring.chat.model.repository.MessageReactionRepository;
import com.kh.spring.chat.model.vo.ChatMessageEntity;
import com.kh.spring.chat.model.vo.ChatRoomEntity;
import com.kh.spring.chat.model.vo.ChatRoomUserEntity;
import com.kh.spring.chat.model.vo.MemberEntity;
import com.kh.spring.chat.model.vo.MessageReactionEntity;

import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ChatServiceImpl implements ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final MemberRepository memberRepository;
    private final ChatRoomUserRepository chatRoomUserRepository;
    private final MessageReactionRepository messageReactionRepository;
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;

    // 채팅방 목록 조회 (참여 방 및 안 읽은 메시지 포함)
    @Override
    @Transactional(readOnly = true)
    public List<ChatRoomDto> selectChatRoomList(Long memberId) {
        List<ChatRoomEntity> chatRooms;
        
        if (memberId != null) {
            // 내가 참여한 방 목록만 조회
            chatRooms = chatRoomRepository.findChatRoomsByMemberId(memberId);
        } else {
            //에러 페이지로
            throw new IllegalArgumentException("회원을 찾을 수 없습니다");
        }

        //채팅방들을 리스트로 반환
        return chatRooms.stream()
                .map(entity -> {
                    int unreadCount = 0;
                    boolean isFavorite = false;
                    String invitationStatus = "ACCEPTED";
                    
                    if (memberId != null) {
                        // 안 읽은 메시지 수 계산
                        unreadCount = countUnreadMessagesOptimized(entity, memberId);
                        
                        // ChatRoomUserEntity에서 즐겨찾기, 초대 상태 조회
                        ChatRoomUserEntity userInfo = chatRoomUserRepository
                            .findByChatRoomIdAndMemberId(entity.getId(), memberId)
                            .orElse(null);
                        
                        if (userInfo != null) {
                            isFavorite = (userInfo.getIsFavorite() != null && userInfo.getIsFavorite() == 1);
                            invitationStatus = userInfo.getInvitationStatus();
                        }
                    }
                    
                    // 참여 인원 수 계산 (PENDING 제외)
                    long count = chatRoomUserRepository.countByChatRoomIdAndInvitationStatusNot(entity.getId(), "PENDING");

                    //채팅방 정보를 dto로 변환 (아래 조건문에서 완성 후 build)
                    ChatRoomDto.ChatRoomDtoBuilder builder = ChatRoomDto.builder()
                            .chatRoomId(entity.getId())
                            .title(entity.getTitle())
                            .roomType(entity.getRoomType())
                            .roomImage(entity.getRoomImage()) 
                            .lastMessageContent(entity.getLastMessageContent())
                            .lastMessageType(entity.getLastMessageType()) 
                            .lastMessageAt(entity.getLastMessageAt())
                            .unreadCount(unreadCount)
                            .memberCount((int) count) // 참여 인원 수 설정
                            .isFavorite(isFavorite)
                            .invitationStatus(invitationStatus);

                    // 1:1 채팅방(SINGLE)이고 상대방 정보를 채워야 하는 경우
                    if ("SINGLE".equals(entity.getRoomType()) && memberId != null) {
                        chatRoomUserRepository.findFirstByChatRoomIdAndMemberIdNot(entity.getId(), memberId)
                            .ifPresent(otherUser -> {
                                builder.otherMemberName(otherUser.getMember().getName());
                                builder.otherMemberProfile(otherUser.getMember().getProfileImageUrl());
                                // 1:1 방의 제목이 비어있으면 상대방 이름으로 설정
                                if (entity.getTitle() == null || entity.getTitle().isEmpty()) {
                                    builder.title(otherUser.getMember().getName());
                                }
                            });
                    }
                    
                    //채팅방 정보를 dto로 변환
                    return builder.build();
                })
                .collect(Collectors.toList());
    }

    // 안 읽은 메시지 갯수 계산 (최적화)
    private int countUnreadMessagesOptimized(ChatRoomEntity chatRoom, Long memberId) {
        // 1. 참여자 정보 조회
        ChatRoomUserEntity roomUser = chatRoomUserRepository.findByChatRoomIdAndMemberId(chatRoom.getId(), memberId)
                .orElse(null);

        if (roomUser == null) {
            return 0;
        }

        // 2. 정확한 안 읽은 메시지 수 계산 (Repository Count Query 사용)
        Long lastReadMessageId = roomUser.getLastReadMessageId();
        
        if (lastReadMessageId == null) {
            // 한 번도 읽은 적이 없으면 전체 메시지 수 반환
            return (int) chatMessageRepository.countByChatRoomId(chatRoom.getId());
        } else {
            // 마지막 읽은 ID 이후의 메시지 수 반환
            return (int) chatMessageRepository.countByChatRoomIdAndIdGreaterThan(chatRoom.getId(), lastReadMessageId);
        }
    }
    
    // 채팅방 생성
    @Override
    public ChatRoomDto createChatRoom(ChatRoomDto roomDto) {
        // 1:1 채팅방이고, 생성자와 맴버가 같으면
        if ("SINGLE".equals(roomDto.getRoomType()) && roomDto.getCreatorId() != null && roomDto.getTargetMemberId() != null) {
             // 두 사용자 간의 1:1 방이 존재하는지 확인
             ChatRoomEntity existingRoom = chatRoomRepository.findExistingSingleRoom(roomDto.getCreatorId(), roomDto.getTargetMemberId())
                     .orElse(null);
             
             if (existingRoom != null) {
                 return ChatRoomDto.builder()
                         .chatRoomId(existingRoom.getId())
                         .title(existingRoom.getTitle())
                         .roomType(existingRoom.getRoomType())
                         .build();
             }
        }
        
        // 없다면 기존 로직대로 새로 생성
        // 1. 채팅방 생성(db 저장)
        ChatRoomEntity entity = ChatRoomEntity.builder()
                .title(roomDto.getTitle())
                .roomType(roomDto.getRoomType())
                .roomImage(roomDto.getRoomImage()) // 이미지 저장
                .createdAt(LocalDateTime.now())
                .build();
        
        ChatRoomEntity saved = chatRoomRepository.save(entity);
        
        // 2. 개설자를 참여자(ChatRoomUser)로 추가
        MemberEntity creator = null;
        if (roomDto.getCreatorId() != null) {
            creator = memberRepository.findById(roomDto.getCreatorId())
                    .orElseThrow(() -> new IllegalArgumentException("생성자를 찾을 수 없습니다"));

            // [Role 정책] 1:1 채팅은 MEMBER, GROUP 채팅은 생성자가 OWNER
            String creatorRole = "SINGLE".equals(roomDto.getRoomType()) ? "MEMBER" : "OWNER";
            
            // 공통 메서드 사용
            addChatRoomUser(saved, creator, creatorRole, "ACCEPTED");
        }

        // 3. 1:1 채팅인 경우 타겟 유저도 바로 참여 처리 (KakaoTalk Style)
        if ("SINGLE".equals(roomDto.getRoomType()) && roomDto.getTargetMemberId() != null) {
             MemberEntity target = memberRepository.findById(roomDto.getTargetMemberId())
                     .orElseThrow(() -> new IllegalArgumentException("대상 회원을 찾을 수 없습니다"));

             // [Role 정책] 1:1 채팅은 둘 다 MEMBER (대등한 관계)
             // [변경] 1:1 채팅도 초대 수락 필요 -> PENDING
             addChatRoomUser(saved, target, "MEMBER", "PENDING");
             
                 // 1:1 채팅 신청 알림 전송 (초대와 동일한 효과) - Transaction Commit 후 전송
             if (creator != null) {
                 ChatNotificationDto notification = ChatNotificationDto.builder()
                        .targetMemberId(target.getId())
                        .type("INVITATION")
                        .chatRoomId(saved.getId())
                        .senderName(creator.getName())
                        .content(creator.getName() + "님이 1:1 대화를 요청했습니다.")
                        .createdAt(LocalDateTime.now())
                        .url("/chat/room/" + saved.getId())
                        .build();
                    
                 eventPublisher.publishEvent(new ChatEvent(target.getId(), notification));
             }
        }

        // 4. [그룹 채팅] 초기 초대 멤버 처리
        if ("GROUP".equals(roomDto.getRoomType()) && roomDto.getInvitedMemberIds() != null && !roomDto.getInvitedMemberIds().isEmpty()) {
            for (Long invitedId : roomDto.getInvitedMemberIds()) {
                // 본인 제외
                if (invitedId.equals(roomDto.getCreatorId())) continue;
                
                try {
                    // 기존 inviteUser 메서드 재사용 (알림 전송 포함)
                    inviteUser(saved.getId(), invitedId, roomDto.getCreatorId());
                } catch (Exception e) {
                    log.warn("그룹 생성 시 초기 초대 실패: memberId={}, error={}", invitedId, e.getMessage());
                }
            }
        }

        // 채팅방 생성 시스템 메시지 추가 (목록 최상단 노출용)
        saveSystemMessage(saved, "채팅방이 생성되었습니다.");
        
        //채팅방 Dto를 반환
        return ChatRoomDto.builder()
                .chatRoomId(saved.getId())
                .title(saved.getTitle())
                .roomType(saved.getRoomType())
                .creatorId(roomDto.getCreatorId())
                .build();
    }

    // [핵심 기능 1] 채팅방 입장 로직
    // 유저가 방 목록에서 특정 방을 클릭하거나, 새로운 방에 초대받아 들어갈 때 실행됩니다.
    @Override
    @CacheEvict(value = "chatRoomDetails", key = "#roomId") // 방명부가 바뀌었으니 기존 캐시(임기저장소)를 날려버려 최신화합니다.
    public void joinChatRoom(Long roomId, Long memberId) {
        
        // 1. 이미 이 방의 명부(ChatRoomUserEntity)에 내 이름이 있는지 DB를 뒤져봅니다.
        ChatRoomUserEntity existingUser = chatRoomUserRepository.findByChatRoomIdAndMemberId(roomId, memberId).orElse(null);
        
        if (existingUser != null) {
            // 2. 이미 내 이름이 명부에 있는데, 아직 수락 안 한 '대기 상태(PENDING)'라면?
            if ("PENDING".equals(existingUser.getInvitationStatus())) {
                
                // [변경점: 그룹 채팅 제한] 단톡방(GROUP)은 "들어갈게요!" 버튼을 눌러야만 들어갈 수 있도록 강제합니다. (자동 끌려감 방지)
                if ("GROUP".equals(existingUser.getChatRoom().getRoomType())) {
                    throw new IllegalArgumentException("초대를 수락해야 입장할 수 있습니다.");
                }

                // 1:1(SINGLE) 갠톡방은 방을 열기만 해도 자동으로 초대를 '수락(ACCEPTED)'한 것으로 처리해버립니다.
                existingUser.setInvitationStatus("ACCEPTED");
                chatRoomUserRepository.save(existingUser);
                
                // 시스템(Admin)의 입을 빌려 "OOO님이 초대를 수락했습니다" 라는 회색 안내 문구를 방 전체에 뿌려줍니다.
                saveSystemMessage(existingUser.getChatRoom(), existingUser.getMember().getName() + "님이 초대를 수락했습니다.");
            }
            return; // 이미 들어와서 처리 끝났으니 여기서 메서드를 종료합니다.
        }

        // 3. 만약 명부에 내 이름이 아예 없다면? (완전 처음 들어오는 상황)
        // 진짜 있는 방이 맞는지, 진짜 있는 회원이 맞는지 한 번 더 깐깐하게 DB에서 조회합니다.
        ChatRoomEntity chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다"));
        
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다"));

        // 4. 이 방의 참여자 명단 테이블(ChatRoomUserEntity)에 내 이름을 새로 써넣습니다. (일반 멤버 자격, 수락 상태)
        addChatRoomUser(chatRoom, member, "MEMBER", "ACCEPTED");
        
        // 5. "OOO님이 들어왔습니다." 라는 입장 환영 회색 시스템 문구를 띄워줍니다.
        saveSystemMessage(chatRoom, member.getName() + "님이 들어왔습니다.");
    }

    // 채팅방 나가기
    @Override
    @CacheEvict(value = "chatRoomDetails", key = "#roomId")
    public void leaveChatRoom(Long roomId, Long memberId) {
        // [동시성 제어] 비관적 락으로 조회 (OWNER 퇴장 시 Race Condition 방지)
        ChatRoomUserEntity roomUser = chatRoomUserRepository.findByChatRoomIdAndMemberIdWithLock(roomId, memberId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방에 참여하고 있지 않습니다"));
        
        ChatRoomEntity chatRoom = roomUser.getChatRoom();
        
        // [방장 탈퇴 정책] GROUP 채팅방에서만 OWNER는 혼자 남은 경우가 아니면 나갈 수 없음 (위임 필수)
        // 1:1 채팅(SINGLE)은 자유롭게 나갈 수 있음
        if ("GROUP".equals(chatRoom.getRoomType()) && "OWNER".equals(roomUser.getRole())) {
            long remainingCount = chatRoomUserRepository.countByChatRoomId(roomId);
            if (remainingCount > 1) {
                throw new IllegalArgumentException("다른 사람에게 방장을 위임하고 나가세요.");
            }
        }
        
        //퇴장 메시지 전송을 위해 멤버 이름 조회
        String memberName = roomUser.getMember().getName();

        //삭제
        chatRoomUserRepository.delete(roomUser);
        chatRoomUserRepository.flush(); // 삭제 내용을 DB에 즉시 반영 (countByChatRoomId 정확도 보장)
        
        // [System Message] 퇴장 메시지 생성
        // [정합성] 마지막 멤버가 나가면 빈 채팅방 삭제 및 시스템 메시지 처리
        long remainingMembers = chatRoomUserRepository.countByChatRoomId(roomId);
        if (remainingMembers == 0) {
            log.info("마지막 멤버 퇴장으로 채팅방 삭제: roomId={}", roomId);
            chatRoomRepository.deleteById(roomId);
        } else {
            // [System Message] 퇴장 메시지 생성 (방이 유지될 때만)
            saveSystemMessage(chatRoom, memberName + "님이 나갔습니다.");
        }
        
        // 나간 사용자에게 목록 갱신 이벤트 전송 (채팅방 목록에서 제거됨)
        Map<String, Object> leaveEvent = new HashMap<>();
        leaveEvent.put("type", "LEAVE_ROOM_SUCCESS");
        leaveEvent.put("chatRoomId", roomId);
        eventPublisher.publishEvent(new ChatEvent(memberId, leaveEvent));
        
        // 남은 사용자들에게 멤버 목록 갱신 이벤트 전송 (채팅방 내부 / 멤버 목록)
        Map<String, Object> updateEvent = new HashMap<>();
        updateEvent.put("type", "MEMBER_UPDATE");
        updateEvent.put("chatRoomId", roomId);
        updateEvent.put("leftMemberId", memberId);
        eventPublisher.publishEvent(new ChatEvent("/topic/chat/room/" + roomId, updateEvent));
        
        // 남은 멤버들에게도 목록 갱신 신호 전송 (채팅 목록의 인원수 갱신용)
        List<ChatRoomUserEntity> remainingUsers = chatRoomUserRepository.findAllByChatRoomId(roomId);
        for (ChatRoomUserEntity user : remainingUsers) {
             Map<String, Object> refreshEvent = new HashMap<>();
             refreshEvent.put("type", "CHAT_LIST_REFRESH");
             eventPublisher.publishEvent(new ChatEvent(user.getMember().getId(), refreshEvent));
        }
    }
    
    // 채팅방 상세 정보 조회
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "chatRoomDetails", key = "#roomId + '_' + (#memberId != null ? #memberId : 'anon')")
    public ChatRoomDto selectChatRoom(Long roomId, Long memberId) {
        // [방어] 채팅방 존재 여부 먼저 확인 - 없으면 404
        ChatRoomEntity chatRoomEntity = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다."));
        
        // [방어] 요청자가 해당 채팅방 멤버인지 확인 - 비멤버면 403
        if (memberId != null) {
            boolean isMember = chatRoomUserRepository.findByChatRoomIdAndMemberId(roomId, memberId).isPresent();
            if (!isMember) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 채팅방에 접근 권한이 없습니다.");
            }
        }
        
        return chatRoomRepository.findById(roomId)
                .map(entity -> {
                    // 참여자 목록 조회 및 DTO 변환
                    List<ChatRoomDto.ParticipantInfo> participants = chatRoomUserRepository
                            .findAllByChatRoomId(roomId).stream()
                            // PENDING 상태인 멤버는 참여자 목록에서 제외 (수락 전까지는 안 보임)
                            .filter(user -> !"PENDING".equals(user.getInvitationStatus())) 
                            .map(roomUser -> ChatRoomDto.ParticipantInfo.builder()
                                    .memberId(roomUser.getMember().getId())
                                    .memberName(roomUser.getMember().getName())
                                    .name(roomUser.getMember().getName()) // 프론트 호환성
                                    .loginId(roomUser.getMember().getLoginId())
                                    .profileImageUrl(roomUser.getMember().getProfileImageUrl())
                                    .role(roomUser.getRole())
                                    .joinedAt(roomUser.getJoinedAt())
                                    .build())
                            .collect(Collectors.toList());
                    
                    // OWNER 찾기
                    Long creatorId = participants.stream()
                            .filter(p -> "OWNER".equals(p.getRole()))
                            .map(ChatRoomDto.ParticipantInfo::getMemberId)
                            .findFirst()
                            .orElse(null);
                    
                    ChatRoomDto dto = ChatRoomDto.builder()
                            .chatRoomId(entity.getId())
                            .creatorId(creatorId) // creatorId 설정
                            .title(entity.getTitle())
                            .roomName(entity.getTitle())  // 프론트엔드 호환성
                            .roomType(entity.getRoomType())
                            .roomImage(entity.getRoomImage()) // [New]
                            .participants(participants)
                            .noticeContent(entity.getNoticeContent())
                            .noticeMessageId(entity.getNoticeMessageId())
                            .build();

                    // 공지 작성자 이름 조회
                    if (entity.getNoticeSenderId() != null) {
                        memberRepository.findById(entity.getNoticeSenderId())
                            .ifPresent(sender -> dto.setNoticeSenderName(sender.getName()));
                    }
                    
                    // 내 마지막 읽은 메시지 ID 설정
                    if (memberId != null) {
                        chatRoomUserRepository.findByChatRoomIdAndMemberId(roomId, memberId)
                            .ifPresent(user -> dto.setMyLastReadMessageId(user.getLastReadMessageId()));
                    }
                    
                    return dto;
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다."));
    }
    
    // [핵심 기능 2] 메시지 저장 로직
    // 누군가 "엔터"를 쳐서 메시지가 백엔드 컨트롤러에 도달하면 이 메서드가 호출됩니다. (가장 많이 실행되는 함수 중 하나)
    @Override
    @Transactional // 도중에 에러가 나면 보낸 메시지를 싹 다 무효화(Rollback)하는 안전마개를 씌웁니다.
    public ChatMessageDto saveMessage(ChatMessageDto messageDto) {
        
        // 1. 누가 어느 '방'에 보냈어? 방 정보 검증
        ChatRoomEntity chatRoom = chatRoomRepository.findById(messageDto.getChatRoomId())
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다"));

        // [보안] 이 사람이 남의 방에서 외부 해킹으로 메시지를 보낸 건 아닌지 '명부'를 확인합니다.
        ChatRoomUserEntity senderInfo = chatRoomUserRepository.findByChatRoomIdAndMemberId(
                messageDto.getChatRoomId(), messageDto.getSenderId())
                .orElseThrow(() -> new IllegalArgumentException("채팅방 멤버만 메시지를 보낼 수 있습니다"));

        // [비즈니스 로직 방어] 아직 갠톡방 상대방이 내 초대를 수락 안 했다면(PENDING) 혼잣말 보내기를 차단합니다.
        if ("SINGLE".equals(chatRoom.getRoomType())) {
            chatRoomUserRepository.findFirstByChatRoomIdAndMemberIdNot(chatRoom.getId(), senderInfo.getMember().getId())
                .ifPresent(otherUser -> {
                    if ("PENDING".equals(otherUser.getInvitationStatus())) {
                        throw new IllegalArgumentException("상대방이 초대를 수락해야 대화를 시작할 수 있습니다.");
                    }
                });
        }

        // 2. 발신자 회원 정보 조회
        MemberEntity sender = memberRepository.findById(messageDto.getSenderId())
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다"));

        // 3. 실제 DB 테이블에 꽂힐 예쁜 엔티티(객체 모형)를 조립(Builder)합니다.
        ChatMessageEntity.ChatMessageEntityBuilder messageBuilder = ChatMessageEntity.builder()
                .chatRoom(chatRoom)
                .sender(sender) 
                .content(messageDto.getContent())
                .messageType(messageDto.getMessageType())
                .createdAt(LocalDateTime.now());
        
        // [답장 특수 기능] 만약 이 메시지가 기존 메시지를 인용(답장)한 거라면?
        if (messageDto.getParentMessageId() != null) {
            ChatMessageEntity parent = chatMessageRepository.findById(messageDto.getParentMessageId())
                    .orElse(null);
            if (parent != null) {
                // [보안] 엉뚱한 방의 남의 메시지를 인용한 건 아닌지 교차 검증합니다.
                if (!parent.getChatRoom().getId().equals(messageDto.getChatRoomId())) {
                    throw new IllegalArgumentException("부모 메시지는 같은 채팅방에 있어야 합니다");
                }
                // 부모 메시지의 꼬리표를 현재 메시지에 달아줍니다.
                messageBuilder.parentMessage(parent);
                
                // 프론트엔드가 인용 부분을 그리기 쉽도록, 보낼 DTO 상자에 원본 내용을 미리 채워 넣어줍니다.
                messageDto.setParentMessageContent(parent.getContent());
                messageDto.setParentMessageSenderName(parent.getSender().getName());
            }
        }
        
        // 조립 완료 후, 진짜 DB에 INSERT 명령을 날립니다!
        ChatMessageEntity messageEntity = messageBuilder.build();
        ChatMessageEntity savedMessage = chatMessageRepository.save(messageEntity);
        
        // 4. 채팅방 겉모습(방 목록)에서 보일 [최근 메시지 1줄 모바일 뷰]를 제가 방금 쓴 글로 갈아끼웁니다.
        // 엄청 여러 명이 0.001초 간격으로 보내더라도 에러가 안 나게끔 재시도 로직(OptimisticLockRetry)을 별도로 돌립니다.
        updateLastMessageWithRetry(chatRoom.getId(), messageDto.getContent(), savedMessage.getCreatedAt(), messageDto.getMessageType());
        
        // 5. 내가 방금 쓴 글이니까, 내 화면에 나 스스로 안 읽은 '1' 표시는 없어야겠죠? 나 스스로의 읽음 카운트를 자동 갱신합니다.
        try {
            updateReadStatus(chatRoom.getId(), sender.getId(), savedMessage.getId());
        } catch (Exception e) {
            log.warn("발신자 자동 읽음 처리 실패: {}", e.getMessage());
            // 읽음 처리에 실패했다고 해서 내 메시지가 전송 안 되면 짜증 나겠죠? 에러만 남기고 정상 진행시킵니다.
        }
        
        // 6. DB에 들어간 실제 ID 번호와 시간 등을 DTO 상자에 예쁘게 담아서 프론트에 넘겨줄 준비를 합니다.
        messageDto.setMessageId(savedMessage.getId());
        messageDto.setCreatedAt(savedMessage.getCreatedAt());
        messageDto.setSenderName(sender.getName());
        messageDto.setSenderProfileImage(sender.getProfileImageUrl());
        
        // 7. 이 글을 아직 안 읽은 사람 수(전체 인원 - 1명(나))를 계산해서 노란색 숫자 카운트에 사용합니다.
        Integer unreadCount = calculateUnreadCount(savedMessage);
        messageDto.setUnreadCount(unreadCount);
        log.info("💬 메시지 저장 완료 - messageId: {}, unreadCount: {}, content: {}", 
            savedMessage.getId(), unreadCount, messageDto.getContent());
        
        // 완성된 상자를 반환하면, Controller가 이걸 받아서 웹소켓으로 푱! 쏩니다.
        return messageDto;
    }
    
    // [동시성 제어] 마지막 메시지 업데이트 (OptimisticLock 재시도)
    @CacheEvict(value = "chatRoomDetails", key = "#roomId")
    public void updateLastMessageWithRetry(Long roomId, String content, LocalDateTime createdAt, String messageType) {
        int maxRetries = 3;
        int retryCount = 0;
        
        while (retryCount < maxRetries) {
            try {
                // 최신 엔티티 다시 조회 (버전 정보 포함)
                ChatRoomEntity chatRoom = chatRoomRepository.findById(roomId)
                        .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다"));
                
                
                chatRoom.updateLastMessage(content, createdAt, messageType); // 타입 전달
                chatRoomRepository.save(chatRoom); // 명시적 save로 즉시 flush
                
                return; // 성공 시 종료
                
            } catch (OptimisticLockException e) {
                retryCount++;
                log.warn("OptimisticLockException 발생 (재시도 {}/{}): roomId={}", retryCount, maxRetries, roomId);
                
                if (retryCount >= maxRetries) {
                    log.error("재시도 한도 초과. 마지막 메시지 업데이트 실패: roomId={}", roomId);
                    // 메시지는 저장되었으므로 예외를 던지지 않고 로그만 남김
                    return;
                }
                
                // 짧은 대기 후 재시도 (exponential backoff)
                try {
                    Thread.sleep(50 * retryCount);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }
    
    private static final int PAGE_SIZE = 30;

    // 채팅방 메시지 내역 조회 (페이징)
    @Override
    public List<ChatMessageDto> selectMessageList(Long roomId, Long cursorId, Long memberId, int limit) {
        // [보안] 참여자만 메시지 조회 가능
        if (memberId != null && chatRoomUserRepository.findByChatRoomIdAndMemberId(roomId, memberId).isEmpty()) {
            throw new IllegalArgumentException("채팅방 멤버만 조회 가능합니다");
        }
        
        // 기본값 및 최대값 설정 (너무 많은 요청 방지)
        if (limit <= 0) limit = 30;
        if (limit > 300) limit = 300; // 최대 300개 제한
        
        Pageable pageable = PageRequest.of(0, limit);
        
        Slice<ChatMessageEntity> messageSlice;

        if (cursorId == null || cursorId == 0) {
            // 커서가 없으면 가장 최신 메시지부터 조회
            messageSlice = chatMessageRepository.findByChatRoomIdOrderByCreatedAtDesc(roomId, pageable);
        } else {
            // 커서(마지막 메시지 ID)보다 작은(이전) 메시지 조회
            messageSlice = chatMessageRepository.findByChatRoomIdAndIdLessThan(roomId, cursorId, pageable);
        }

        //Dto 객체로 변환
        return messageSlice.getContent().stream()
                .map(entity -> convertToDto(entity, memberId))
                // DB에서 최신순(DESC)으로 가져왔으므로, 화면에 뿌릴 때는 다시 과거순(ASC)으로 뒤집어서 출력
                .sorted(Comparator.comparing(ChatMessageDto::getCreatedAt)) 
                .collect(Collectors.toList());
    }

    // 메시지 검색 (키워드/페이징)
    @Override
    public List<ChatMessageDto> searchMessages(Long roomId, Long memberId, String keyword, int limit, int offset) {
        log.info("🔍 [메시지 검색] roomId: {}, keyword: {}, limit: {}, offset: {}", roomId, keyword, limit, offset);
        
        // 1. 참여 여부 확인
        chatRoomUserRepository.findByChatRoomIdAndMemberId(roomId, memberId)
            .orElseThrow(() -> new IllegalArgumentException("채팅방 참여자가 아닙니다."));

        // 2. 검색 (CLOB 검색 호환성을 위해 이름 변경됨)
        List<ChatMessageEntity> allMessages = chatMessageRepository
                .findByChatRoomIdAndContentContainingOrderByCreatedAtDesc(roomId, keyword);
        
        // 3. 페이징 적용 (offset부터 limit개만큼)
        List<ChatMessageEntity> paginatedMessages = allMessages.stream()
                .skip(offset)
                .limit(limit)
                .collect(Collectors.toList());
        
        return paginatedMessages.stream()
                .map(entity -> convertToDto(entity, memberId))
                .collect(Collectors.toList());
    }

    // [Helper] Entity -> DTO 변환
    private ChatMessageDto convertToDto(ChatMessageEntity entity, Long memberId) {
        // 리액션 가공
        List<ChatMessageDto.ReactionSummary> reactionSummaries = entity.getReactions().stream()
            .collect(Collectors.groupingBy(MessageReactionEntity::getEmojiType))
            .entrySet().stream()
            .map(entry -> {
                String emoji = entry.getKey();
                List<MessageReactionEntity> list = entry.getValue();
                
                boolean me = false;
                if (memberId != null) {
                    me = list.stream().anyMatch(r -> r.getMember().getId().equals(memberId));
                }
                
                return ChatMessageDto.ReactionSummary.builder()
                        .emojiType(emoji)
                        .count(list.size())
                        .selectedByMe(me)
                        .build();
            })
            .collect(Collectors.toList());

        // [개선] 읽음 수 계산
        Integer unreadCount = calculateUnreadCount(entity);

        // 삭제된 메시지 처리 (DB에는 TEXT로 저장되나, 조회 시 DELETED로 변환)
        String messageType = entity.getMessageType();
        if ("삭제된 메시지입니다".equals(entity.getContent())) {
            messageType = "DELETED";
        }

        ChatMessageDto.ChatMessageDtoBuilder builder = ChatMessageDto.builder()
            .messageId(entity.getId())
            .chatRoomId(entity.getChatRoom().getId())
            .senderId(entity.getSender().getId())
            .senderName(entity.getSender().getName())
            .senderProfileImage(entity.getSender().getProfileImageUrl())
            .content(entity.getContent())
            .messageType(messageType) // 수정된 타입 적용
            .createdAt(entity.getCreatedAt())
            .reactions(reactionSummaries)
            .unreadCount(unreadCount);



        // [답장/인용] 부모 메시지 정보 채우기
        if (entity.getParentMessage() != null) {
            builder.parentMessageId(entity.getParentMessage().getId());
            builder.parentMessageContent(entity.getParentMessage().getContent());
            builder.parentMessageSenderName(entity.getParentMessage().getSender().getName());
        }

        return builder.build();
    }
    
    // [Helper] 메시지별 안 읽은 사람 수 계산 (전체 유저 DB 조회)
    private Integer calculateUnreadCount(ChatMessageEntity message) {
        try {
            Long chatRoomId = message.getChatRoom().getId();
            // 전체 사용자 조회 (DB Query)
            List<ChatRoomUserEntity> allUsers = chatRoomUserRepository.findAllByChatRoomId(chatRoomId);
            return calculateUnreadCount(message, allUsers);
        } catch (Exception e) {
            log.warn("읽음 수 계산 실패: {}", e.getMessage());
            return null;
        }
    }

    // [Helper] 메시지별 안 읽은 사람 수 계산 (미리 조회된 유저 목록 사용 - 최적화)
    private Integer calculateUnreadCount(ChatMessageEntity message, List<ChatRoomUserEntity> allUsers) {
        try {
            Long messageId = message.getId();
            Long senderId = message.getSender().getId();
            
            // 발신자 제외한 참여자 수 (PENDING 제외)
            long totalRecipients = allUsers.stream()
                .filter(u -> !u.getMember().getId().equals(senderId))
                .filter(u -> !"PENDING".equals(u.getInvitationStatus())) // PENDING 사용자 제외
                .count();
            
            // 읽은 사람 수 계산 (lastReadMessageId >= 현재 메시지 ID, PENDING 제외)
            long readCount = allUsers.stream()
                .filter(u -> !u.getMember().getId().equals(senderId))
                .filter(u -> !"PENDING".equals(u.getInvitationStatus())) // PENDING 사용자 제외 (안전장치)
                .filter(u -> {
                    Long lastReadId = u.getLastReadMessageId();
                    // lastReadMessageId가 null이면 아직 하나도 안 읽은 것이므로(0), 현재 messageId보다 작음(<)
                    return lastReadId != null && lastReadId >= messageId;
                })
                .count();
            
            // 안 읽은 사람 수
            int unread = (int) (totalRecipients - readCount);
            return unread > 0 ? unread : null;
        } catch (Exception e) {
            log.warn("읽음 수 계산 실패 (Optimized): {}", e.getMessage());
            return null; 
        }
    }

    // [핵심 기능 3] 메시지 읽음 처리 (안 읽은 개수 까내리기)
    // 유저가 방에 들어갔을 때, 스크롤을 내렸을 때 빈번하게 호출되어 읽음 상태를 최신화시킵니다.
    @Override
    @CacheEvict(value = "chatRoomDetails", key = "#roomId + '_' + #memberId") // "나 읽었어!" 라고 신고하면 내 방 정보 캐시를 비웁니다.
    public void updateReadStatus(Long roomId, Long memberId, Long lastMessageId) {
        
        // 1. 방 참여 명부에서 내 정보를 꺼내옵니다.
        ChatRoomUserEntity roomUser = chatRoomUserRepository.findByChatRoomIdAndMemberId(roomId, memberId)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 채팅방에 없습니다"));
        
        ChatRoomEntity chatRoom = roomUser.getChatRoom();
        Long currentTotalCount = chatRoom.getTotalMessageCount(); // 이 방의 평생 메시지 누적 샌드백 개수
        if (currentTotalCount == null) {
            currentTotalCount = 0L;
        }

        // 2. 만약 프론트가 "몇 번 메시지까지 읽었는지" 안 알려주고 그냥 방을 열기만 했다면? (lastMessageId == null)
        // 가장 최신 메시지의 번호를 DB에서 캐온 뒤 "아 너 그냥 안 밀리고 다 읽은 걸로 쳐줄게!" 라고 퉁칩니다.
        if (lastMessageId == null) {
             Optional<ChatMessageEntity> lastMsg = chatMessageRepository.findFirstByChatRoomIdOrderByCreatedAtDesc(roomId);
             if (lastMsg.isPresent()) {
                 lastMessageId = lastMsg.get().getId();
             } else {
                 return; // 애초에 방에 보낸 메시지가 0개면 읽음 처리 할 것도 없이 나갑니다.
             }
        }

        // 3. 내가 과거에 '마지막으로 읽었다'고 도장 찍었던 메시지 고유 번호
        Long oldLastReadId = roomUser.getLastReadMessageId();

        // [중요 보안] 실수나 프론트 버그로 "야 나 어제 메시지 1번까지만 읽었어" 로 옛날 정보를 보낼 경우, 
        // 100번까지 다 본 내 정보가 뒤로 되돌려져서(백섭) 안 읽은 카운터가 폭주할 수 있습니다. 이를 막아줍니다.
        if (oldLastReadId != null && lastMessageId <= oldLastReadId) {
            return;
        }

        // 4. "나 이 번호까지 다 읽었다!!" 라고 명부에 도장을 찍어줍니다.
        roomUser.updateLastReadMessageId(lastMessageId);
        
        // (보조) 1차원적인 숫자로 안읽음개수 뺄셈을 하기 위한 카운터도 같이 갱신해줍니다.
        roomUser.updateLastReadMessageCount(currentTotalCount);
        
        // DB 테이블에 즉시 반영 (Flush)
        chatRoomUserRepository.saveAndFlush(roomUser);

        
        // 5. 프론트엔드가 노란색 '1' 배지를 실시간으로 깎아내리기 위해 어떤 메시지의 배지가 까졌는지 계산합니다.
        // 내가 이번에 새롭게 '읽음' 처리로 추가된 구간의 메시지들을 DB에서 긁어모읍니다.
        List<ChatMessageEntity> affectedMessages;
        if (oldLastReadId == null) {
             // 처음 방에 들어온 뉴비: 0번부터 내가 본 곳까지 싹 다
             affectedMessages = chatMessageRepository.findByChatRoomIdAndIdLessThanEqual(roomId, lastMessageId);
        } else {
             // 기존 유저: "전에 읽었던 번호(old)" 초과 ~ "지금 읽은 번호(last)" 이하의 메시지들
             affectedMessages = chatMessageRepository.findByChatRoomIdAndIdGreaterThanAndIdLessThanEqual(
                     roomId, oldLastReadId, lastMessageId);
        }
        
        // 성능 향상(최적화) 포인트: 방 멤버 100명 명단을 매번 메시지마다 DB에서 긁지 않고, 한 방에 긁어다 둡니다.
        List<ChatRoomUserEntity> allUsers = chatRoomUserRepository.findAllByChatRoomId(roomId);

        // Map 통에 담기: [메시지 10번 = 안읽은사람 2명], [메시지 11번 = 안읽은사람 3명] ...
        Map<Long, Integer> unreadCountMap = new HashMap<>();
        for (ChatMessageEntity message : affectedMessages) {
            Integer count = calculateUnreadCount(message, allUsers);
            unreadCountMap.put(message.getId(), count);
        }
        
        // 6. 이 귀중한 정보(누가 얼마나 숫자가 깎였는지 맵)를 모조리 담아서 웹소켓 '이벤트 봉투'에 넣고 쏩니다!
        Map<String, Object> readEvent = new HashMap<>();
        readEvent.put("type", "READ_UPDATE");
        readEvent.put("memberId", memberId); // 내가 읽었소!
        readEvent.put("lastMessageId", lastMessageId);
        readEvent.put("unreadCountMap", unreadCountMap);  // 메시지별 바뀐 노란 딱지 개수표
        
        // 이 방에 있는 모든 화면에 뿌려버립니다! (노란 딱지가 뿅 하고 실시간으로 사라지게 됨)
        eventPublisher.publishEvent(new ChatEvent("/topic/chat/room/" + roomId + "/read", readEvent));
        
        // 7. 내 채팅방 목록(바깥 화면)에서도 "안 읽은 개수 0표시" 최신화를 하라며 나 자신에게 1:1 이벤트를 쏩니다.
        Map<String, Object> listRefreshEvent = new HashMap<>();
        listRefreshEvent.put("type", "CHAT_LIST_REFRESH");
        listRefreshEvent.put("chatRoomId", roomId);
        eventPublisher.publishEvent(new ChatEvent(memberId, listRefreshEvent));
        
        // 8. 발신자가 방 밖(목록 화면)에 있더라도 자기가 쓴 글 숫자 깎이는 걸 알려주기 위해, 다른 팀원들에게도 개인 톡을 보냅니다.
        for (ChatRoomUserEntity user : allUsers) {
            Long userId = user.getMember().getId();
            if (!userId.equals(memberId)) { // 읽은 본인은 제외!
                Map<String, Object> personalReadEvent = new HashMap<>();
                personalReadEvent.put("type", "READ_UPDATE");
                personalReadEvent.put("memberId", memberId);
                personalReadEvent.put("lastMessageId", lastMessageId);
                personalReadEvent.put("chatRoomId", roomId);
                personalReadEvent.put("unreadCountMap", unreadCountMap);
                eventPublisher.publishEvent(new ChatEvent(userId, personalReadEvent));
            }
        }
        
        log.debug("읽음 상태 실시간 이벤트 전송: roomId={}, memberId={}, lastMessageId={}, affectedMessages={}", 
                roomId, memberId, lastMessageId, affectedMessages.size());
    }

    // [Helper] 시스템 메시지 저장 및 전송
    private void saveSystemMessage(ChatRoomEntity chatRoom, String content) {
        log.info("🟢 [시스템 메시지] 저장 시작 - chatRoomId: {}, content: {}", chatRoom.getId(), content);
        
        // 시스템 메시지는 발신자가 없으므로(null) '시스템' 더미 계정을 만들어서 사용
        // Admin 계정을 찾아서 넣는 로직으로 구현.(id가 1인 멤버가 admin)
        MemberEntity systemSender = memberRepository.findById(1L)
                 .orElse(null); 
        
        if (systemSender == null) {
            // [개선] Admin 계정이 없으면 시스템 메시지를 건너뜀 (에러 발생 방지)
            log.warn("❌ System Account (ID=1) not found. System message skipped: {}", content);
            return;
        }
        
        log.info("✅ System Account 찾음: {}", systemSender.getName());

        // [예외 처리] 시스템 메시지 저장 실패 시에도 전체 트랜잭션은 유지
        try {
            ChatMessageEntity systemMessage = ChatMessageEntity.builder()
                    .chatRoom(chatRoom)
                    .sender(systemSender)
                    .content(content)
                    .messageType("TEXT") // [수정] DB 제약조건(CHK_MSG_TYPE) 위배 방지를 위해 TEXT로 변경
                    .createdAt(LocalDateTime.now())
                    .build();
            
            ChatMessageEntity saved = chatMessageRepository.save(systemMessage);
            log.info("💾 DB 저장 완료 - messageId: {}", saved.getId());
            
            // ✅ WebSocket으로 실시간 전송 추가
            ChatMessageDto messageDto = ChatMessageDto.builder()
                    .messageId(saved.getId())
                    .chatRoomId(chatRoom.getId())
                    .senderId(systemSender.getId())
                    .senderName("시스템")
                    .content(content)
                    .messageType("SYSTEM") // [변경] 프론트엔드에서 시스템 메시지로 인식하도록 SYSTEM 타입 전송
                    .createdAt(saved.getCreatedAt())
                    .build();
        
            String topic = "/topic/chat/room/" + chatRoom.getId(); // 변수 선언 위치 수정
            log.info("📡 WebSocket 전송 - topic: {}, messageDto: {}", topic, messageDto);
            
            eventPublisher.publishEvent(new ChatEvent(topic, messageDto));
            
            // 시스템 메시지도 채팅방의 '마지막 메시지'로 업데이트하여 목록 최상단으로 올림
            updateLastMessageWithRetry(chatRoom.getId(), content, saved.getCreatedAt(), "SYSTEM");
            
            log.info("✅ 시스템 메시지 저장 및 전송 완료!");
            
        } catch (Exception e) {
            log.error("❌ Failed to save system message: {}", content, e);
        }
    }
    
    // 글로벌 알림 전송 (비동기)
    @Override
    @Transactional(readOnly = true)
    public void sendGlobalNotifications(ChatMessageDto savedMessage) {
        // LazyInitializationException 방지: DB 조회 및 엔티티 접근은 트랜잭션 내에서 수행
        List<ChatRoomUserEntity> users = chatRoomUserRepository.findAllByChatRoomId(savedMessage.getChatRoomId());
        
        // 미리 알림 DTO 리스트 생성 (비동기 스레드로 엔티티를 넘기지 않음)
        List<ChatNotificationDto> notifications = users.stream()
            .filter(user -> !user.getMember().getId().equals(savedMessage.getSenderId())) // 본인 제외
            .map(user -> {
                // 채팅방 이름 가져오기 (여기서는 트랜잭션 안이므로 Lazy Loading 가능)
                String roomTitle = user.getChatRoom().getTitle();
                if (roomTitle == null || roomTitle.isEmpty()) {
                    roomTitle = "채팅방";
                }
                
                return ChatNotificationDto.builder()
                    .targetMemberId(user.getMember().getId())
                    .type("CHAT")
                    .chatRoomId(savedMessage.getChatRoomId())
                    .senderName(savedMessage.getSenderName())
                    .senderProfileImage(savedMessage.getSenderProfileImage())
                    .content(savedMessage.getContent())
                    .messageType(savedMessage.getMessageType() != null ? savedMessage.getMessageType() : "TEXT")
                    .roomName(roomTitle)
                    .createdAt(LocalDateTime.now())
                    .url("/chat/room/" + savedMessage.getChatRoomId())
                    .build();
            })
            .collect(Collectors.toList());

        // 실제 전송 - 현재 트랜잭션(@Transactional readOnly)이 살아있으므로
        // eventPublisher.publishEvent로 AFTER_COMMIT 처리 가능
        // 단, CompletableFuture.runAsync 안에서 발행하면 새 스레드엔 트랜잭션X → AFTER_COMMIT 무시됨
        // 따라서 현재 트랜잭션 컨텍스트에서 동기적으로 발행
        for (ChatNotificationDto notification : notifications) {
            eventPublisher.publishEvent(new ChatEvent(notification.getTargetMemberId(), notification));
        }
    }

    // 메시지 리액션 토글
    @Override
    public void toggleReaction(Long messageId, Long memberId, String emojiType) {
        // 1. 메시지 존재 확인
        ChatMessageEntity message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("메시지를 찾을 수 없습니다"));

        // [보안] 메시지가 속한 채팅방의 멤버인지 확인
        Long chatRoomId = message.getChatRoom().getId();
        if (chatRoomUserRepository.findByChatRoomIdAndMemberId(chatRoomId, memberId).isEmpty()) {
            throw new IllegalArgumentException("채팅방 멤버만 리액션을 남길 수 있습니다");
        }

        // 2. 멤버 존재 확인
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다"));

        // 3. 기존 리액션 확인
        Optional<MessageReactionEntity> existingReactionOpt = 
                messageReactionRepository.findByChatMessageIdAndMemberId(messageId, memberId);

        String action = "";
        if (existingReactionOpt.isPresent()) {
            MessageReactionEntity existingReaction = existingReactionOpt.get();
            
            if (existingReaction.getEmojiType().equals(emojiType)) {
                // 3-1. 같은 이모지면 삭제 (토글 Off)
                messageReactionRepository.delete(existingReaction);
                messageReactionRepository.flush(); // 즉시 반영
                action = "REMOVE";
            } else {
                // 3-2. 반대 이모지면 업데이트 (변경)
                existingReaction.updateEmoji(emojiType);
                messageReactionRepository.saveAndFlush(existingReaction); // 즉시 반영
                action = "UPDATE";
            }
        } else {
            // 4. 없으면 신규 생성
            MessageReactionEntity reaction = MessageReactionEntity.builder()
                    .chatMessage(message)
                    .member(member)
                    .emojiType(emojiType)
                    .build();
            
            messageReactionRepository.saveAndFlush(reaction); // Flush to ensure DB update
            action = "ADD";
        }
        
        // 실시간 갱신: "누가 어떻게 반응했는지" 상세 정보 전송
        // 프론트엔드에서 '내가 누른 것'을 즉시 식별하여 UI를 갱신할 수 있도록 함
        
        // 최신 리액션 카운트 정보 조회 (DTO 변환 활용)
        ChatMessageDto updatedMsg = convertToDto(message, null);
        
        Map<String, Object> reactionEvent = new HashMap<>();
        reactionEvent.put("type", "REACTION_UPDATE");
        reactionEvent.put("messageId", messageId);
        reactionEvent.put("reactorId", memberId); // 반응한 사람 ID
        reactionEvent.put("action", action);      // ADD, REMOVE, UPDATE
        reactionEvent.put("emojiType", emojiType);
        reactionEvent.put("reactions", updatedMsg.getReactions()); // 갱신된 카운트 목록
        
        eventPublisher.publishEvent(new ChatEvent("/topic/chat/room/" + chatRoomId + "/reaction", reactionEvent));
        log.debug("공감 실시간 이벤트 전송 (Detailed): roomId={}, messageId={}, reactorId={}, action={}", 
                chatRoomId, messageId, memberId, action);
    }

    // 권한 변경 (방장 위임 등)
    @Override
    public void updateRole(Long chatRoomId, Long targetMemberId, Long requesterId, String newRole) {
        log.info("🔄 [역할 변경 요청] chatRoomId: {}, targetMemberId: {}, newRole: {}, requesterId: {}", 
                chatRoomId, targetMemberId, newRole, requesterId);
        
        // [보안] 자기 자신에게 권한 변경 불가
        if (targetMemberId.equals(requesterId)) {
            throw new IllegalArgumentException("자기 자신의 권한은 변경할 수 없습니다");
        }
        
        // 1. 요청자 권한 확인
        ChatRoomUserEntity requester = chatRoomUserRepository.findByChatRoomIdAndMemberId(chatRoomId, requesterId)
                .orElseThrow(() -> new IllegalArgumentException("요청자가 채팅방에 없습니다"));
        
        // [정책] 1:1 채팅방에서는 역할 변경 불가
        if ("SINGLE".equals(requester.getChatRoom().getRoomType())) {
            throw new IllegalArgumentException("1:1 채팅방에서는 역할 변경이 불가능합니다");
        }
        
        if (!"OWNER".equals(requester.getRole())) {
             throw new IllegalArgumentException("방장만 권한을 변경할 수 있습니다");
        }
        
        // 2. 대상 조회
        ChatRoomUserEntity target = chatRoomUserRepository.findByChatRoomIdAndMemberId(chatRoomId, targetMemberId)
                .orElseThrow(() -> new IllegalArgumentException("대상 사용자가 채팅방에 없습니다"));

        // 3. 로직 처리
        log.info("🎯 [역할 변경 분기] newRole: {}", newRole);
        
        // 만약 방장을 위임하는 경우 (OWNER -> MEMBER, Target -> OWNER)
        if ("OWNER".equals(newRole)) {
            log.info("👑 방장 위임 처리 시작");
            // 기존 방장은 MEMBER로 강등
            requester.setRole("MEMBER");
            target.setRole("OWNER");
            
            // 시스템 메시지
            log.info("📢 시스템 메시지 호출: 방장 위임");
            saveSystemMessage(requester.getChatRoom(), requester.getMember().getName() + "님이 방장을 위임했습니다.");
        } else if ("ADMIN".equals(newRole)) {
            log.info("⬆️ 관리자 승격 처리 시작");
            // 관리자로 승격
            target.setRole("ADMIN");
            log.info("📢 시스템 메시지 호출: 관리자 승격");
            saveSystemMessage(requester.getChatRoom(), target.getMember().getName() + "님이 관리자로 승격되었습니다.");
        } else if ("MEMBER".equals(newRole)) {
            log.info("⬇️ 일반 멤버 강등 처리 시작");
            // 일반 멤버로 강등
            target.setRole("MEMBER");
            log.info("📢 시스템 메시지 호출: 일반 멤버 강등");
            saveSystemMessage(requester.getChatRoom(), target.getMember().getName() + "님이 일반 멤버로 강등되었습니다.");
        } else {
            throw new IllegalArgumentException("유효하지 않은 역할입니다: " + newRole);
        }
        
        // 역할 변경 이벤트 전송 (멤버 목록 갱신용)
        Map<String, Object> updateEvent = new HashMap<>();
        updateEvent.put("type", "MEMBER_UPDATE"); // ChatRoomDetail에서 수신 시 멤버 목록 재조회
        updateEvent.put("chatRoomId", chatRoomId);
        updateEvent.put("targetMemberId", targetMemberId);
        updateEvent.put("newRole", newRole);
        
        eventPublisher.publishEvent(new ChatEvent("/topic/chat/room/" + chatRoomId, updateEvent));
        
        log.info("✅ 역할 변경 완료 & 이벤트 전송");
    }

    // 멤버 강퇴
    @Override
    public void kickMember(Long chatRoomId, Long targetMemberId, Long requesterId) {
        // [보안] 자기 자신을 강퇴할 수 없음
        if (targetMemberId.equals(requesterId)) {
            throw new IllegalArgumentException("자기 자신을 강퇴할 수 없습니다");
        }
        
        // 1. 요청자 조회
        ChatRoomUserEntity requester = chatRoomUserRepository.findByChatRoomIdAndMemberId(chatRoomId, requesterId)
                .orElseThrow(() -> new IllegalArgumentException("요청자가 채팅방에 없습니다"));
        
        // [정책] 1:1 채팅방에서는 강퇴 불가
        if ("SINGLE".equals(requester.getChatRoom().getRoomType())) {
            throw new IllegalArgumentException("1:1 채팅방에서는 강퇴가 불가능합니다");
        }
        
        // 2. 권한 확인 (OWNER나 ADMIN만 강퇴 가능)
        if (!"OWNER".equals(requester.getRole()) && !"ADMIN".equals(requester.getRole())) {
            throw new IllegalArgumentException("강퇴 권한이 없습니다");
        }
        
        // 3. 대상 조회
        ChatRoomUserEntity target = chatRoomUserRepository.findByChatRoomIdAndMemberId(chatRoomId, targetMemberId)
                .orElseThrow(() -> new IllegalArgumentException("대상 사용자가 채팅방에 없습니다"));
        
        // (옵션) 방장은 강퇴 불가
        if ("OWNER".equals(target.getRole())) {
            throw new IllegalArgumentException("방장을 강퇴할 수 없습니다");
        }

        // 4. 강퇴 (삭제)
        String targetName = target.getMember().getName();
        chatRoomUserRepository.delete(target);
        chatRoomUserRepository.flush(); // 삭제 내용을 DB에 즉시 반영 (countByChatRoomId 정확도 보장)
        
        // 강퇴 알림 전송 (대상에게)
        String roomTitle = requester.getChatRoom().getTitle();
        if (roomTitle == null || roomTitle.isEmpty()) {
            roomTitle = "채팅방";
        }
        
        ChatNotificationDto notification = ChatNotificationDto.builder()
                .targetMemberId(targetMemberId)
                .type("KICK")
                .chatRoomId(chatRoomId)
                .content(roomTitle + " 채팅방에서 강퇴당했습니다.")
                .createdAt(LocalDateTime.now())
                .build();
        
        eventPublisher.publishEvent(new ChatEvent(targetMemberId, notification));
        
        // 5. 시스템 메시지
        saveSystemMessage(requester.getChatRoom(), targetName + "님이 강퇴당했습니다.");
        
        // 6. [정합성] 강퇴 후 방이 비었으면 삭제 (leaveChatRoom과 일관성 유지)
        long remainingMembers = chatRoomUserRepository.countByChatRoomId(chatRoomId);
        if (remainingMembers == 0) {
            log.info("강퇴 후 빈 채팅방 삭제: roomId={}", chatRoomId);
            chatRoomRepository.deleteById(chatRoomId);
        }
    }
    
    // ===================================
    // 메시지 삭제 (Soft Delete)
    // ===================================
    
    @Override
    public void softDeleteMessage(Long messageId, Long memberId, Long requesterId) {
        // 1. 메시지 조회
        ChatMessageEntity message = chatMessageRepository.findById(messageId)
            .orElseThrow(() -> new IllegalArgumentException("메시지를 찾을 수 없습니다"));
        
        // 2. [보안] 작성자 본인 또는 방장 확인
        boolean isSender = java.util.Objects.equals(message.getSender().getId(), memberId);
        boolean isRoomOwner = false;
        if (requesterId != null) {
            ChatRoomUserEntity requester = chatRoomUserRepository
                .findByChatRoomIdAndMemberId(message.getChatRoom().getId(), requesterId)
                .orElse(null);
            isRoomOwner = requester != null && "OWNER".equals(requester.getRole());
        }
        if (!isSender && !isRoomOwner) {
            log.error("삭제 권한 없음: senderId={}, requesterId={}", message.getSender().getId(), memberId);
            throw new IllegalArgumentException("삭제 권한이 없습니다");
        }
        
        // 3. Soft Delete: content 및 messageType 변경
        message.setContent("삭제된 메시지입니다");
        message.setMessageType("TEXT"); // DB 제약조건(CHK_MSG_TYPE) 준수
        chatMessageRepository.saveAndFlush(message); // Flush to ensure DB update before broadcast
        
        // 4. WebSocket으로 실시간 전파
        ChatMessageDto dto = convertEntityToDto(message, memberId); // Use new helper method
        eventPublisher.publishEvent(new ChatEvent("/topic/chat/room/" + message.getChatRoom().getId(), dto));
        
        log.info("메시지 삭제 완료: messageId={}, memberId={}", messageId, memberId);
    }
    
    // ===================================
    // 채팅방 공지 관리
    // ===================================
    
    @Override
    @CacheEvict(value = "chatRoomDetails", key = "#roomId") // 캐시 무효화 추가
    public void setNotice(Long roomId, Long memberId, Long messageId) {
        // 1. 권한 확인 (모든 멤버 가능)
        ChatRoomUserEntity requester = chatRoomUserRepository.findByChatRoomIdAndMemberId(roomId, memberId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방 멤버만 공지를 설정할 수 있습니다"));
        
        // 2. 메시지 확인
        ChatMessageEntity message = chatMessageRepository.findById(messageId)
            .orElseThrow(() -> new IllegalArgumentException("메시지를 찾을 수 없습니다"));
        
        // 3. [보안] 메시지가 해당 채팅방에 속해 있는지 확인
        if (!message.getChatRoom().getId().equals(roomId)) {
            throw new IllegalArgumentException("다른 채팅방의 메시지를 공지로 설정할 수 없습니다");
        }
        
        // 4. 공지 설정
        ChatRoomEntity room = requester.getChatRoom();
        room.setNoticeContent(message.getContent());
        room.setNoticeMessageId(messageId);
        room.setNoticeSenderId(memberId); // 작성자 ID 저장
        chatRoomRepository.save(room);
        
        // 5. WebSocket으로 공지 변경 이벤트 전송
        Map<String, Object> noticeEvent = new HashMap<>();
        noticeEvent.put("type", "NOTICE_UPDATED");
        noticeEvent.put("noticeContent", message.getContent());
        noticeEvent.put("noticeMessageId", messageId);
        noticeEvent.put("senderName", requester.getMember().getName()); // 작성자 이름
        noticeEvent.put("senderId", memberId);
        // 프론트엔드가 구독 중인 메인 토픽으로 전송
        eventPublisher.publishEvent(new ChatEvent("/topic/chat/room/" + roomId, noticeEvent));
        
        log.info("공지 설정: roomId={}, messageId={}, memberId={}", roomId, messageId, memberId);
    }
    
    @Override
    @CacheEvict(value = "chatRoomDetails", key = "#roomId") // 캐시 무효화 추가
    public void clearNotice(Long roomId, Long memberId) {
        // 1. 권한 확인 (모든 멤버 가능 - 정책에 따름, 일단 유지하거나 풉니다. 요청사항은 "공지는 모두가")
        // -> 공지 해제도 모두가 가능한가? 보통 내리기는 관리자 권한이거나 본인이 올린 것만 가능하지만, 
        // 요청사항 "공지는 방장이 아니라 모든 사람이 할 수 있게"를 "관리"까지 포함으로 해석하여 풉니다.
        ChatRoomUserEntity roomUser = chatRoomUserRepository
            .findByChatRoomIdAndMemberId(roomId, memberId)
            .orElseThrow(() -> new IllegalArgumentException("채팅방에 참여하고 있지 않습니다"));
        
        // 2. 공지 해제
        ChatRoomEntity room = roomUser.getChatRoom();
        room.setNoticeContent(null);
        room.setNoticeMessageId(null);
        room.setNoticeSenderId(null); // 초기화
        chatRoomRepository.save(room);
        
        // 3. WebSocket 이벤트 전송
        Map<String, Object> noticeEvent = new HashMap<>();
        noticeEvent.put("type", "NOTICE_CLEARED");
        // 프론트엔드가 구독 중인 메인 토픽으로 전송
        eventPublisher.publishEvent(new ChatEvent("/topic/chat/room/" + roomId, noticeEvent));
        
        log.info("공지 해제: roomId={}, memberId={}", roomId, memberId);
    }
    
    // 채팅방 즐겨찾기 토글
    @Override
    public void toggleFavorite(Long roomId, Long memberId) {
        log.info("⭐ [즐겨찾기 토글] roomId: {}, memberId: {}", roomId, memberId);
        
        // 1. 참여자 정보 조회
        ChatRoomUserEntity roomUser = chatRoomUserRepository
            .findByChatRoomIdAndMemberId(roomId, memberId)
            .orElseThrow(() -> new IllegalArgumentException("채팅방에 참여하고 있지 않습니다"));
        
        // 2. 즐겨찾기 토글
        roomUser.toggleFavorite();
        chatRoomUserRepository.save(roomUser);
        
        log.info("✅ 즐겨찾기 변경 완료: isFavorite={}", roomUser.getIsFavorite());
    }
    
    // 사용자 초대
    @Override
    public void inviteUser(Long roomId, Long invitedMemberId, Long requesterId) {
        log.info("📧 [사용자 초대] roomId: {}, invitedMemberId: {}, requesterId: {}", 
                roomId, invitedMemberId, requesterId);
        
        // 1. 초대자 권한 확인
        ChatRoomUserEntity requester = chatRoomUserRepository
            .findByChatRoomIdAndMemberId(roomId, requesterId)
            .orElseThrow(() -> new IllegalArgumentException("채팅방에 참여하고 있지 않습니다"));
        
        // 2. 그룹 채팅방인지 확인
        if ("SINGLE".equals(requester.getChatRoom().getRoomType())) {
            throw new IllegalArgumentException("1:1 채팅방에는 초대할 수 없습니다");
        }
        
        // 3. 이미 참여 중인지 확인
        Optional<ChatRoomUserEntity> existing = chatRoomUserRepository
            .findByChatRoomIdAndMemberId(roomId, invitedMemberId);
        
        if (existing.isPresent()) {
            throw new IllegalArgumentException("이미 채팅방에 참여 중인 사용자입니다");
        }
        
        // 4. 초대받을 사용자 조회
        MemberEntity invitedMember = memberRepository.findById(invitedMemberId)
            .orElseThrow(() -> new IllegalArgumentException("초대할 사용자를 찾을 수 없습니다"));
        
        // 5. PENDING 상태로 참여자 추가 (공통 메서드 사용)
        addChatRoomUser(requester.getChatRoom(), invitedMember, "MEMBER", "PENDING");
        
        // 실시간 알림 전송 (초대받은 사람에게) - [Fix] 트랜잭션 커밋 후 전송 (Event 사용)
        String roomTitle = requester.getChatRoom().getTitle();
        String inviteTargetName = (roomTitle != null && !roomTitle.isEmpty()) ? roomTitle : "채팅방";
        
        ChatNotificationDto notification = ChatNotificationDto.builder()
                .targetMemberId(invitedMemberId)
                .type("INVITATION")
                .chatRoomId(roomId)
                .senderName(requester.getMember().getName())
                .content(requester.getMember().getName() + "님이 " + inviteTargetName + "에 초대했습니다.")
                .createdAt(LocalDateTime.now())
                .url("/chat/room/" + roomId) // 클릭 시 이동할 경로 (바로 입장되지는 않고, Accept 필요)
                .build();
            
        eventPublisher.publishEvent(new ChatEvent(invitedMemberId, notification));
        
        log.info("✅ 초대 완료 (Event Published): invitedMemberId={}, status=PENDING", invitedMemberId);
        
        // 5. 시스템 메시지 전송: "OO님이 OO님을 초대했습니다"
        saveSystemMessage(requester.getChatRoom(), requester.getMember().getName() + "님이 " + invitedMember.getName() + "님을 " + inviteTargetName + "에 초대했습니다.");
    }
    
    // 초대 수락
    @Override
    public void acceptInvitation(Long roomId, Long memberId) {
        log.info("✅ [초대 수락] roomId: {}, memberId: {}", roomId, memberId);
        
        // 1. 참여자 정보 조회
        ChatRoomUserEntity roomUser = chatRoomUserRepository
            .findByChatRoomIdAndMemberId(roomId, memberId)
            .orElseThrow(() -> new IllegalArgumentException("초대 정보를 찾을 수 없습니다"));
        
        // 2. PENDING 상태인지 확인
        if (!"PENDING".equals(roomUser.getInvitationStatus())) {
            throw new IllegalArgumentException("처리할 수 있는 초대가 아닙니다");
        }
        
        // 3. 상태를 ACCEPTED로 변경
        roomUser.setInvitationStatus("ACCEPTED");
        chatRoomUserRepository.save(roomUser);
        
        // 4. 시스템 메시지 전송: "OO님이 들어왔습니다"
        String memberName = roomUser.getMember().getName();
        saveSystemMessage(roomUser.getChatRoom(), memberName + "님이 들어왔습니다.");
        
        // 5. 멤버 변경 이벤트 전송 (실시간 목록 갱신용)
        Map<String, Object> memberEvent = new HashMap<>();
        memberEvent.put("type", "MEMBER_UPDATE");
        memberEvent.put("chatRoomId", roomId);
        
        eventPublisher.publishEvent(new ChatEvent("/topic/chat/room/" + roomId, memberEvent));
        
        log.info("✅ 초대 수락 완료: memberId={}, memberName={}", memberId, memberName);
    }
    
    // 초대 거절
    @Override
    public void rejectInvitation(Long roomId, Long memberId) {
        log.info("❌ [초대 거절] roomId: {}, memberId: {}", roomId, memberId);
        
        // 1. 참여자 정보 조회
        ChatRoomUserEntity roomUser = chatRoomUserRepository
            .findByChatRoomIdAndMemberId(roomId, memberId)
            .orElseThrow(() -> new IllegalArgumentException("초대 정보를 찾을 수 없습니다"));
        
        // 2. PENDING 상태인지 확인
        if (!"PENDING".equals(roomUser.getInvitationStatus())) {
            throw new IllegalArgumentException("처리할 수 있는 초대가 아닙니다");
        }
        
        // 3. 참여 레코드 삭제 (거절 시엔 레코드를 제거)
        chatRoomUserRepository.delete(roomUser);
        
        log.info("✅ 초대 거절 완료: memberId={}, 레코드 삭제됨", memberId);
        
        // 4. 시스템 메시지 전송: "OO님이 초대를 거절했습니다"
        String memberName = roomUser.getMember().getName();
        saveSystemMessage(roomUser.getChatRoom(), memberName + "님이 초대를 거절했습니다.");
    }

    // 프로필 이미지 변경 (채팅 전용)
    @Override
    @Transactional
    public void updateProfile(Long memberId, String profileImageUrl) {
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다"));
        
        member.updateProfileImage(profileImageUrl);
        log.info("🖼️ 프로필 이미지 업데이트 완료: memberId={}, url={}", memberId, profileImageUrl);
        
        // 프로필 변경 이벤트 전송 (본인 및 관련 사용자들에게 갱신 요청)
        Map<String, Object> profileEvent = new HashMap<>();
        profileEvent.put("type", "PROFILE_UPDATE");
        profileEvent.put("memberId", memberId);
        profileEvent.put("profileImageUrl", profileImageUrl);
        
        // 1. 본인에게 전송 (다른 기기/탭 동기화)
        eventPublisher.publishEvent(new ChatEvent(memberId, profileEvent));
        
        // 2. (선택적) 이 사용자가 속한 채팅방들에도 알릴 수 있음.
        // 하지만 지금은 클라이언트가 채팅방 목록/상세 진입 시 이미지를 로드하므로,
        // 본인 클라이언트가 갱신되는 것이 가장 중요함.
        // 필요하다면, 채팅방 내의 실시간 갱신을 위해 채팅방 토픽으로도 쏠 수 있음.
        // 여기서는 "내 프로필이 바뀌었다"는 것을 "나"에게 알려주는 것에 집중.
    }

    @Override
    @Transactional(readOnly = true)
    // 채팅방 멤버 목록 조회
    public List<ChatMemberDto> getChatRoomMembers(Long chatRoomId) {
        return chatRoomUserRepository.findAllByChatRoomId(chatRoomId).stream()
                // PENDING 상태인 멤버는 목록에서 제외
                .filter(user -> !"PENDING".equals(user.getInvitationStatus()))
                .map(user -> ChatMemberDto.builder()
                        .memberId(user.getMember().getId())
                        .name(user.getMember().getName())
                        .loginId(user.getMember().getLoginId())
                        .profileImageUrl(user.getMember().getProfileImageUrl())
                        .role(user.getRole())
                        .joinedAt(user.getJoinedAt())
                        .build())
                .collect(Collectors.toList());
    }

    // [Helper] 채팅방 유저(ChatRoomUser) 추가
    private ChatRoomUserEntity addChatRoomUser(ChatRoomEntity chatRoom, MemberEntity member, String role, String invitationStatus) {
        ChatRoomUserEntity roomUser = ChatRoomUserEntity.builder()
                .chatRoom(chatRoom)
                .member(member)
                .role(role)
                .invitationStatus(invitationStatus)
                .joinedAt(LocalDateTime.now())
                .lastReadMessageId(0L)
                .lastReadMessageCount(0L)
                .isFavorite(0)
                .build();
        return chatRoomUserRepository.save(roomUser);
    }
    
    // 회원 검색 (이름/닉네임)
    @Override
    @Transactional(readOnly = true)
    public List<ChatMemberDto> searchMember(String keyword) {
        log.info("🔍 [멤버 검색] keyword(정확일치): {}", keyword);
        List<MemberEntity> members = memberRepository.findByName(keyword);
        log.info("✅ [멤버 검색 결과] 갯수: {}", members.size());
        
        return members.stream()
                .map(member -> ChatMemberDto.builder()
                        .memberId(member.getId())
                        .name(member.getName())
                        .loginId(member.getLoginId())
                        .profileImageUrl(member.getProfileImageUrl())
                        .build())
                .collect(Collectors.toList());
    }

    // 채팅방 이름 변경 (방장 전용)
    @Override
    @Transactional
    public void updateRoomTitle(Long roomId, Long memberId, String newTitle) {
        log.info("📝 [방 이름 변경 요청] roomId: {}, memberId: {}, newTitle: {}", roomId, memberId, newTitle);
        
        ChatRoomEntity chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다."));

        // 권한 확인: 요청자가 해당 방의 OWNER인지 확인
        ChatRoomUserEntity roomUser = chatRoomUserRepository.findByChatRoomIdAndMemberId(roomId, memberId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방에 참여하고 있지 않습니다."));
        
        log.info("🔎 [권한 확인] 사용자 Role: {}", roomUser.getRole());
        
        if (!"OWNER".equals(roomUser.getRole())) {
            log.warn("❌ [권한 거부] 방장(OWNER)만 변경 가능. 현재 권한: {}", roomUser.getRole());
            throw new IllegalArgumentException("방장만 채팅방 설정을 변경할 수 있습니다.");
        }
        
        chatRoom.setTitle(newTitle);
        chatRoomRepository.save(chatRoom);
        
        // 시스템 메시지 전송 (DB: TEXT, WebSocket: SYSTEM)
        String content = "채팅방 이름이 '" + newTitle + "'(으)로 변경되었습니다.";
        saveSystemMessage(chatRoom, content);
        
        // 방 정보 업데이트 이벤트 전송 (프론트엔드 타이틀 갱신용)
        Map<String, Object> updateEvent = new HashMap<>();
        updateEvent.put("type", "ROOM_UPDATE");
        updateEvent.put("chatRoomId", roomId);
        updateEvent.put("title", newTitle);
        
        eventPublisher.publishEvent(new ChatEvent("/topic/chat/room/" + roomId, updateEvent));
        
        // 모든 참여자에게 목록 갱신 신호 전송 (개인 채널)
        List<ChatRoomUserEntity> roomUsers = chatRoomUserRepository.findAllByChatRoomId(roomId);
        for (ChatRoomUserEntity user : roomUsers) {
             Map<String, Object> refreshEvent = new HashMap<>();
             refreshEvent.put("type", "CHAT_LIST_REFRESH");
             refreshEvent.put("chatRoomId", roomId);
             eventPublisher.publishEvent(new ChatEvent(user.getMember().getId(), refreshEvent));
        }
        
        log.info("✅ [방 이름 변경 완료] roomId: {}", roomId);
    }

    // 채팅방 이미지 변경 (방장 전용)
    @Override
    @Transactional
    public void updateRoomImage(Long roomId, Long memberId, String imageUrl) {
        ChatRoomEntity chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다."));

        // 권한 체크 (방장만 가능)
        ChatRoomUserEntity roomUser = chatRoomUserRepository.findByChatRoomIdAndMemberId(roomId, memberId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방 참여자가 아닙니다."));

        if (!"OWNER".equals(roomUser.getRole())) {
            throw new IllegalArgumentException("방장만 채팅방 이미지를 변경할 수 있습니다.");
        }

        // 이미지 업데이트
        chatRoom.setRoomImage(imageUrl);
        chatRoomRepository.save(chatRoom);

        // 시스템 메시지 전송 (DB: TEXT, WebSocket: SYSTEM)
        String content = "채팅방 이미지가 변경되었습니다.";
        saveSystemMessage(chatRoom, content);
        
        // 방 정보 업데이트 이벤트 전송 (이미지 갱신용)
        Map<String, Object> updateEvent = new HashMap<>();
        updateEvent.put("type", "ROOM_UPDATE");
        updateEvent.put("chatRoomId", roomId);
        updateEvent.put("roomImage", imageUrl);
        
        eventPublisher.publishEvent(new ChatEvent("/topic/chat/room/" + roomId, updateEvent));
        
        // 모든 참여자에게 목록 갱신 신호 전송 (개인 채널)
        List<ChatRoomUserEntity> roomUsers = chatRoomUserRepository.findAllByChatRoomId(roomId);
        for (ChatRoomUserEntity user : roomUsers) {
             Map<String, Object> refreshEvent = new HashMap<>();
             refreshEvent.put("type", "CHAT_LIST_REFRESH");
             refreshEvent.put("chatRoomId", roomId);
             eventPublisher.publishEvent(new ChatEvent(user.getMember().getId(), refreshEvent));
        }
        
        log.info("✅ [방 이미지 변경 완료] roomId: {}", roomId);
    }

    // [Helper] Entity -> DTO 변환 (삭제 처리 포함)
    private ChatMessageDto convertEntityToDto(ChatMessageEntity entity, Long memberId) {
        String messageType = entity.getMessageType();
        
        // "삭제된 메시지입니다" 내용을 가진 경우 타입을 DELETED로 강제 변환 (DB에는 TEXT로 저장되더라도)
        if ("삭제된 메시지입니다".equals(entity.getContent())) {
            messageType = "DELETED";
        }

        return ChatMessageDto.builder()
                .messageId(entity.getId())
                .chatRoomId(entity.getChatRoom().getId())
                .senderId(entity.getSender().getId())
                .senderName(entity.getSender().getName())
                .senderProfileImage(entity.getSender().getProfileImageUrl())
                .content(entity.getContent())
                .messageType(messageType)
                .createdAt(entity.getCreatedAt())
                .unreadCount(0) // 기본값, 필요 시 별도 계산 로직 추가
                .parentMessageContent(entity.getParentMessage() != null ? entity.getParentMessage().getContent() : null)
                .parentMessageSenderName(entity.getParentMessage() != null ? entity.getParentMessage().getSender().getName() : null)
                .build();
    }
    
    // 초대 중인 사용자 목록 조회
    @Override
    @Transactional(readOnly = true)
    public List<ChatMemberDto> getInvitedUsers(Long chatRoomId) {
        return chatRoomUserRepository.findAllByChatRoomId(chatRoomId).stream()
                .filter(user -> "PENDING".equals(user.getInvitationStatus()))
                .map(user -> ChatMemberDto.builder()
                        .memberId(user.getMember().getId())
                        .name(user.getMember().getName())
                        .loginId(user.getMember().getLoginId())
                        .profileImageUrl(user.getMember().getProfileImageUrl())
                        .role(user.getRole()) // MEMBER
                        .build())
                .collect(Collectors.toList());
    }

    // 초대 취소 (회수)
    @Override
    @Transactional
    public void cancelInvitation(Long chatRoomId, Long targetMemberId, Long requesterId) {
        // 1. 요청자 권한 확인
        ChatRoomUserEntity requester = chatRoomUserRepository.findByChatRoomIdAndMemberId(chatRoomId, requesterId)
                .orElseThrow(() -> new IllegalArgumentException("요청자가 채팅방에 없습니다"));
        
        // 방장(OWNER) 또는 관리자(ADMIN)만 초대 취소 가능
        if (!"OWNER".equals(requester.getRole()) && !"ADMIN".equals(requester.getRole())) {
            throw new IllegalArgumentException("초대를 취소할 권한이 없습니다 (방장/관리자만 가능)");
        }
        
        // 2. 대상 확인 (PENDING 상태여야 함)
        ChatRoomUserEntity target = chatRoomUserRepository.findByChatRoomIdAndMemberId(chatRoomId, targetMemberId)
                .orElseThrow(() -> new IllegalArgumentException("대상 사용자가 초대상태가 아닙니다"));
        
        if (!"PENDING".equals(target.getInvitationStatus())) {
            throw new IllegalArgumentException("이미 수락하거나 거절한 초대는 취소할 수 없습니다");
        }
        
        // 3. 초대 데이터 삭제
        String targetName = target.getMember().getName();
        chatRoomUserRepository.delete(target);
        
        // 4. 실시간 알림 전송 (대상에게 - 목록에서 사라지도록)
        // LEAVE_ROOM_SUCCESS 타입을 재활용하거나 INVITATION_CANCELLED 이벤트 추가
        Map<String, Object> cancelEvent = new HashMap<>();
        cancelEvent.put("type", "LEAVE_ROOM_SUCCESS"); // 목록 갱신 트리거
        cancelEvent.put("chatRoomId", chatRoomId);
        
        eventPublisher.publishEvent(new ChatEvent(targetMemberId, cancelEvent));
        
        // 5. 시스템 메시지 등은 선택적 (여기선 생략하거나 로그만)
        log.info("🚫 초대 취소 완료: target={}, requester={}", targetName, requester.getMember().getName());
    }
}