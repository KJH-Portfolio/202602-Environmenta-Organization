package com.kh.spring.chat.controller;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.kh.spring.chat.model.dto.ChatMessageDto;
import com.kh.spring.chat.model.dto.ChatNotificationDto;
import com.kh.spring.chat.model.dto.ChatRoomDto;
import com.kh.spring.chat.model.dto.ChatTypingDto;
import com.kh.spring.chat.model.service.ChatService;
import com.kh.spring.chat.model.vo.ChatRoomUserEntity;
import com.kh.spring.util.ChatFileUtil;
import java.util.Arrays;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/chat")
@Tag(name = "Chat", description = "채팅 관련 API")
@RequiredArgsConstructor
@Slf4j
// 채팅 기능 컨트롤러
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    // 회원 검색 (닉네임 부분 일치)
    @Operation(summary = "회원 검색 (닉네임)", description = "닉네임으로 회원을 검색합니다. (부분 일치)")
    @GetMapping("/users/search")
    public ResponseEntity<List<java.util.Map<String, Object>>> searchMember(@RequestParam String keyword) {
        List<com.kh.spring.chat.model.dto.ChatMemberDto> members = chatService.searchMember(keyword);
        
        List<java.util.Map<String, Object>> responseList = new java.util.ArrayList<>();
        for (com.kh.spring.chat.model.dto.ChatMemberDto member : members) {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("memberId", member.getMemberId());
            map.put("name", member.getName());
            map.put("loginId", member.getLoginId());
            map.put("profileImageUrl", member.getProfileImageUrl());
            responseList.add(map);
        }
        
        return ResponseEntity.ok(responseList);
    }

    // ======================================================================
    // 1. 실시간 채팅 (WebSocket/STOMP)
    // ======================================================================
    
    // [웹소켓 진입점] 프론트엔드가 "/app/chat/message" 목적지로 메시지를 쏘면 이 메서드가 낚아챕니다.
    @MessageMapping("/chat/message")
    public void sendMessage(ChatMessageDto messageDto) {
        log.info("메시지 수신: {}", messageDto);
        
        try {
            // 1. DB에 메시지 먼저 안전하게 저장합니다. (누가 보냈는지, 내용은 뭔지 테이블에 INSERT)
            ChatMessageDto savedMessage = chatService.saveMessage(messageDto);
            log.info("✅ 저장된 메시지 - messageId: {}, unreadCount: {}", 
                savedMessage.getMessageId(), savedMessage.getUnreadCount());
            
            // 2. 메시지가 DB에 잘 저장되었다면, 이 방("/topic/chat/room/방번호")을 구독(쳐다보고)하고 있는 모든 화면(프론트)에 메시지를 쏴줍니다.
            messagingTemplate.convertAndSend("/topic/chat/room/" + messageDto.getChatRoomId(), savedMessage);
            
            // 3. 메시지 발송과는 별개로, '안 읽음 뱃지'나 '푸시 알람'등을 처리하기 위한 이벤트를 백그라운드에서 동작시킵니다.
            chatService.sendGlobalNotifications(savedMessage);
            
        } catch (IllegalArgumentException e) {
            log.error("메시지 전송 실패 (유효성 검증): {}", e.getMessage());
            
            // [에러 처리 1] 만약 방에 없는 회원이거나, 글자수가 넘쳤을 때 등 에러가 나면
            // 방 전체가 아니라, 메시지를 '보냈던 사람 한 명'에게만 1:1로 에러 사유를 전송해서 팝업을 띄우게 합니다.
            ChatMessageDto errorMsg = ChatMessageDto.builder()
                    .messageType("ERROR") // 에러 타입 명시
                    .content(e.getMessage())
                    .chatRoomId(messageDto.getChatRoomId())
                    .build();
            messagingTemplate.convertAndSend("/topic/user/" + messageDto.getSenderId(), errorMsg);
            
        } catch (Exception e) {
            log.error("메시지 전송 중 알 수 없는 오류 발생", e);
            
             // [에러 처리 2] DB가 죽었거나 알 수 없는 서버 에러일 때도 마찬가지로 당사자에게 에러를 알립니다.
             ChatMessageDto errorMsg = ChatMessageDto.builder()
                    .messageType("ERROR") 
                    .content("메시지 전송 중 오류가 발생했습니다.")
                    .chatRoomId(messageDto.getChatRoomId())
                    .build();
            messagingTemplate.convertAndSend("/topic/user/" + messageDto.getSenderId(), errorMsg);
        }
    }

    // ======================================================================
    // 2. REST API (Swagger에 노출됨)
    // ======================================================================

    // 채팅방 목록 조회
    @Operation(summary = "채팅방 목록 조회", description = "채팅방 목록을 조회합니다. memberId가 있으면 해당 회원이 참여한 방만 조회하고 안 읽은 메시지 수도 계산합니다.")
    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomDto>> getChatRoomList(@RequestParam(required = false) Long memberId) {
        return ResponseEntity.ok(chatService.selectChatRoomList(memberId));
    }

    // 채팅방 참여
    @Operation(summary = "채팅방 참여", description = "채팅방에 참여합니다.")
    @PostMapping("/room/{roomId}/join")
    public ResponseEntity<Void> joinChatRoom(@PathVariable Long roomId, @RequestParam Long memberId) {
        chatService.joinChatRoom(roomId, memberId);
        return ResponseEntity.ok().build();
    }

    // 채팅방 나가기
    @Operation(summary = "채팅방 나가기", description = "채팅방에서 나갑니다.")
    @DeleteMapping("/room/{roomId}/leave")
    public ResponseEntity<String> leaveChatRoom(@PathVariable Long roomId, @RequestParam Long memberId) {
        try {
            chatService.leaveChatRoom(roomId, memberId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("채팅방 나가기 중 알 수 없는 오류 발생", e);
            return ResponseEntity.internalServerError().body("오류 발생: " + e.getClass().getName() + " - " + e.getMessage());
        }
    }

    // 채팅방 생성
    @Operation(summary = "채팅방 생성", description = "새로운 채팅방을 생성합니다.")
    @PostMapping("/room")
    public ResponseEntity<ChatRoomDto> createChatRoom(@RequestBody ChatRoomDto roomDto) {
        return ResponseEntity.ok(chatService.createChatRoom(roomDto));
    }

    @Operation(summary = "채팅방 상세/입장", description = "특정 채팅방의 정보를 조회합니다.")
    @GetMapping("/room/{roomId}")
    public ResponseEntity<ChatRoomDto> getChatRoom(@PathVariable Long roomId, @RequestParam(required = false) Long memberId) {
        return ResponseEntity.ok(chatService.selectChatRoom(roomId, memberId));
    }
    
    // 채팅방 메시지 내역 조회
    @Operation(summary = "채팅방 메시지 조회", description = "특정 채팅방의 메시지를 조회합니다.")
    @GetMapping("/room/{roomId}/messages")
    public ResponseEntity<List<ChatMessageDto>> getMessageList(
            @PathVariable Long roomId, 
            @RequestParam(required = false) Long cursorId,
            @RequestParam(required = false) Long memberId,
            @RequestParam(defaultValue = "30") int limit) {
        return ResponseEntity.ok(chatService.selectMessageList(roomId, cursorId, memberId, limit));
    }
    
    // 메시지 읽음 처리 (마지막 읽은 ID 갱신)
    @Operation(summary = "메시지 읽음 처리", description = "특정 채팅방의 모든 메시지를 읽음 처리합니다 (마지막 읽은 메시지 ID 갱신).")
    @PostMapping("/room/{roomId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long roomId, @RequestParam Long memberId, @RequestParam(required = false) Long lastMessageId) {
        chatService.updateReadStatus(roomId, memberId, lastMessageId);
        return ResponseEntity.ok().build();
    }
    
    // 메시지 리액션(공감) 토글
    @Operation(summary = "메시지 리액션(공감) 토글", description = "특정 메시지에 공감을 남기거나 취소/변경합니다.")
    @PostMapping("/message/{messageId}/reaction")
    public ResponseEntity<Void> toggleReaction(
            @PathVariable Long messageId, 
            @RequestParam Long memberId, 
            @RequestParam String emojiType) {
        chatService.toggleReaction(messageId, memberId, emojiType);
        return ResponseEntity.ok().build();
    }

    // 채팅방 멤버 목록 조회
    @Operation(summary = "채팅방 멤버 조회", description = "특정 채팅방의 참여자 목록을 조회합니다.")
    @GetMapping("/room/{roomId}/members")
    public ResponseEntity<List<com.kh.spring.chat.model.dto.ChatMemberDto>> getChatRoomMembers(@PathVariable Long roomId) {
        return ResponseEntity.ok(chatService.getChatRoomMembers(roomId));
    }
    
    // ===================================
    // 3. 파일 업로드 (멀티미디어)
    // ===================================
    
    private final ChatFileUtil chatFileUtil; 

    // 파일 업로드 (이미지 및 일반 파일)
    // 채팅에서 "사진전송" 버튼을 누르면 웹소켓이 아니라 1회성 HTTP 통신으로 이 메서드가 실행됩니다. (멀티파트 폼 데이터 방식)
    @Operation(summary = "채팅 파일 업로드", description = "이미지/파일을 서버 하드디스크에 저장하고, 꺼내볼 수 있는 URL 주소를 문자열로 응답합니다.")
    @PostMapping(value = "/upload", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // [보안 검증 1] 파일 크기 제한 (10MB 이상 업로드 방지하여 서버 터짐 예방)
            if (file.getSize() > 10 * 1024 * 1024) {
                return ResponseEntity.badRequest().body("파일 크기는 10MB를 초과할 수 없습니다");
            }
            
            // [보안 검증 2] 파일명 자체가 없는 깡통 파일인지 검사
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                return ResponseEntity.badRequest().body("파일명이 유효하지 않습니다");
            }
            
            // [보안 검증 3] .jpg, .png 처럼 점(.)이 없는 파일 거르기
            if (!originalFilename.contains(".")) {
                return ResponseEntity.badRequest().body("파일 확장자가 필요합니다");
            }
            
            // [보안 검증 4] 관리자(개발자)가 허락한 안전한 확장자만 화이트리스트 검사 (해킹 파일.exe 등 원천 차단)
            String ext = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            List<String> allowedExts = Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".pdf", ".txt", ".zip");
            if (!allowedExts.contains(ext)) {
                return ResponseEntity.badRequest().body("허용되지 않는 파일 확장자입니다");
            }
            
            // [보안 검증 5] 파일의 실제 내용물(MIME TYPE) 검증 (이름만 .jpg로 바꾼 바이러스 방지)
            String contentType = file.getContentType();
            if (contentType == null) {
                return ResponseEntity.badRequest().body("파일 타입을 확인할 수 없습니다");
            }
            
            List<String> allowedMimes = Arrays.asList(
                "image/jpeg", "image/png", "image/gif",  // 이미지류
                "application/pdf",                        // 문서류
                "text/plain",                             
                "application/zip", "application/x-zip-compressed"  // 압축류
            );
            
            if (!allowedMimes.contains(contentType)) {
                return ResponseEntity.badRequest().body("허용되지 않는 파일 형식입니다 (MIME: " + contentType + ")");
            }
            
            // 모든 검문소를 통과했다면, 실제 하드디스크의 "chat/message" 폴더에 저장하라고 유틸 클래스에 위임합니다.
            String savedFileName = chatFileUtil.saveFile(file, "chat/message");
            
            // 프론트엔드가 <img src="/chat/file/message/어쩌구저쩌구.jpg"> 로 화면에 띄울 수 있도록 경로 문자열만 반환합니다.
            String fileUrl = "/chat/file/message/" + savedFileName;
            
            return ResponseEntity.ok(fileUrl);
            
        } catch (Exception e) {
            log.error("File Upload Failed", e);
            return ResponseEntity.internalServerError().body("Upload Failed");
        }
    }
    
    // ===================================
    // 5. 그룹 관리 (Role & Kick)
    // ===================================

    // 채팅방 권한 변경 (방장 위임)
    @Operation(summary = "채팅방 권한 변경 (방장 위임)", description = "방장이 다른 멤버에게 방장을 위임하거나 권한을 변경합니다.")
    @PatchMapping("/room/{roomId}/user/{memberId}/role")
    public ResponseEntity<Void> updateRole(
            @PathVariable Long roomId,
            @PathVariable Long memberId,
            @RequestParam Long requesterId,
            @RequestParam String newRole) {
        chatService.updateRole(roomId, memberId, requesterId, newRole);
        return ResponseEntity.ok().build();
    }

    // 멤버 강퇴
    @Operation(summary = "멤버 강퇴", description = "방장 또는 관리자가 멤버를 강퇴합니다.")
    @DeleteMapping("/room/{roomId}/user/{memberId}")
    public ResponseEntity<Void> kickMember(
            @PathVariable Long roomId,
            @PathVariable Long memberId,
            @RequestParam Long requesterId) {
        chatService.kickMember(roomId, memberId, requesterId);
        return ResponseEntity.ok().build();
        }

    // 메시지 검색 (키워드)
    @Operation(summary = "메시지 검색", description = "채팅방 내 메시지를 키워드로 검색합니다 (최신순, 페이징 지원).")
    @GetMapping("/room/{roomId}/search")
    public ResponseEntity<List<ChatMessageDto>> searchMessages(
            @PathVariable Long roomId,
            @RequestParam Long memberId,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        return ResponseEntity.ok(chatService.searchMessages(roomId, memberId, keyword, limit, offset));
    }

    // ===================================
    // 4. 입력 상태 표시 (Typing Indicator)
    // ===================================
    
    // 입력 중 상태 표시 (WebSocket)
    @MessageMapping("/chat/typing")
    public void typing(ChatTypingDto typingDto) {
        // 클라이언트 구독 경로: /topic/chat/room/{roomId}/typing
        messagingTemplate.convertAndSend("/topic/chat/room/" + typingDto.getChatRoomId() + "/typing", typingDto);
    }
    
    // ===================================
    // 6. 메시지 삭제 (Soft Delete)
    // ===================================
    
    // 메시지 삭제 (Soft Delete)
    @Operation(summary = "메시지 삭제", description = "작성자 또는 방장이 메시지를 삭제합니다. (Soft Delete)")
    @PutMapping("/message/{messageId}/delete")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable Long messageId, 
            @RequestParam Long memberId,
            @RequestParam(required = false) Long requesterId) {
        chatService.softDeleteMessage(messageId, memberId, requesterId);
        return ResponseEntity.ok().build();
    }
    
    // ===================================
    // 7. 채팅방 공지 관리
    // ===================================
    
    // 채팅방 공지 설정
    @Operation(summary = "채팅방 공지 설정", description = "방장 또는 관리자가 특정 메시지를 공지로 설정합니다.")
    @PostMapping("/room/{roomId}/notice")
    public ResponseEntity<Void> setNotice(
            @PathVariable Long roomId, 
            @RequestParam Long memberId,
            @RequestParam Long messageId) {
        chatService.setNotice(roomId, memberId, messageId);
        return ResponseEntity.ok().build();
    }
    
    // 채팅방 공지 해제
    @Operation(summary = "채팅방 공지 해제", description = "방장 또는 관리자가 채팅방 공지를 해제합니다.")
    @DeleteMapping("/room/{roomId}/notice")
    public ResponseEntity<Void> clearNotice(
            @PathVariable Long roomId, 
            @RequestParam Long memberId) {
        chatService.clearNotice(roomId, memberId);
        return ResponseEntity.ok().build();
    }
    
    // 채팅방 즐겨찾기 토글
    @PutMapping("/rooms/{roomId}/favorite")
    public ResponseEntity<Void> toggleFavorite(
            @PathVariable Long roomId,
            @RequestParam Long memberId) {
        chatService.toggleFavorite(roomId, memberId);
        return ResponseEntity.ok().build();
    }
    
    // 사용자 초대
    @PostMapping("/rooms/{roomId}/invite")
    public ResponseEntity<Void> inviteUser(
            @PathVariable Long roomId,
            @RequestParam Long invitedMemberId,
            @RequestParam Long requesterId) {
        chatService.inviteUser(roomId, invitedMemberId, requesterId);
        return ResponseEntity.ok().build();
    }
    
    // 초대 수락
    @PutMapping("/rooms/{roomId}/invitation/accept")
    public ResponseEntity<Void> acceptInvitation(
            @PathVariable Long roomId,
            @RequestParam Long memberId) {
        chatService.acceptInvitation(roomId, memberId);
        return ResponseEntity.ok().build();
    }
    
    // 초대 거절
    @PutMapping("/rooms/{roomId}/invitation/reject")
    public ResponseEntity<Void> rejectInvitation(
            @PathVariable Long roomId,
            @RequestParam Long memberId) {
        chatService.rejectInvitation(roomId, memberId);
        return ResponseEntity.ok().build();
    }
    
    // 초대 중인 사용자 목록 조회
    @Operation(summary = "초대 중인 사용자 조회", description = "채팅방에 초대되었으나 아직 수락하지 않은 사용자 목록을 조회합니다.")
    @GetMapping("/rooms/{roomId}/invitations")
    public ResponseEntity<List<com.kh.spring.chat.model.dto.ChatMemberDto>> getInvitedUsers(@PathVariable Long roomId) {
        return ResponseEntity.ok(chatService.getInvitedUsers(roomId));
    }

    // 초대 취소 (회수)
    @Operation(summary = "초대 취소", description = "방장 또는 관리자가 보낸 초대를 취소합니다.")
    @DeleteMapping("/rooms/{roomId}/invitations/{targetMemberId}")
    public ResponseEntity<Void> cancelInvitation(
            @PathVariable Long roomId,
            @PathVariable Long targetMemberId,
            @RequestParam Long requesterId) {
        chatService.cancelInvitation(roomId, targetMemberId, requesterId);
        return ResponseEntity.ok().build();
    }
    // 프로필 이미지 변경 (채팅 전용)
    @Operation(summary = "프로필 이미지 변경", description = "사용자의 프로필 이미지를 변경합니다. (채팅 전용)")
    @PatchMapping("/user/profile")
    public ResponseEntity<Void> updateProfile(
            @RequestParam Long memberId,
            @RequestParam String profileImageUrl) {
        chatService.updateProfile(memberId, profileImageUrl);
        return ResponseEntity.ok().build();
    }
    // 채팅방 이름 변경 (방장 전용)
    @Operation(summary = "채팅방 이름 변경", description = "방장이 채팅방의 이름을 변경합니다.")
    @PatchMapping("/room/{roomId}/title")
    public ResponseEntity<Void> updateRoomTitle(
            @PathVariable Long roomId,
            @RequestParam Long memberId,
            @RequestParam String newTitle) {
        chatService.updateRoomTitle(roomId, memberId, newTitle);
        return ResponseEntity.ok().build();
    }

    // 채팅방 이미지 변경 (방장 전용)
    @Operation(summary = "채팅방 이미지 변경", description = "방장이 채팅방의 이미지를 변경합니다.")
    @PatchMapping("/room/{roomId}/image")
    public ResponseEntity<Void> updateRoomImage(
            @PathVariable Long roomId,
            @RequestParam Long memberId,
            @RequestParam String imageUrl) {
        chatService.updateRoomImage(roomId, memberId, imageUrl);
        return ResponseEntity.ok().build();
    }
}
