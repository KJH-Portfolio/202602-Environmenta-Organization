package com.kh.spring.chat.event;

import lombok.Getter;

// [이벤트 메일 봉투] 역할을 하는 객체입니다.
// Service 단에서 이 객체를 만들어서 "발송!" 하면 별도의 리스너(ChatEventListener)가 받아서 처리합니다.
// 왜 굳이 바로 안 쏘고 객체를 만드나요? -> 서비스(비즈니스) 로직과 알림(소켓) 로직을 서로 분리해서 코드를 깔끔하게 유지하기 위해서입니다.
@Getter // 값들을 꺼내 읽을 수 있게 Getter 편의 메서드 생성
public class ChatEvent {

    private final Long targetMemberId; // 알림을 받을 특정 한 명의 유저 번호 (그룹 채팅방 전체에 쏠 때는 이게 필요 없으므로 null)
    private final String destination;  // 데이터가 날아갈 웹소켓 구독 주소 (예: 특정 유저는 "/topic/user/1", 방 전체는 "/topic/chat/room/100")
    private final Object payload;      // 실제로 전송할 "알짜배기 데이터" 본문 내용 (주로 Dto 객체나 Map 등이 통째로 들어감)

    // [생성자 1] '특정 방 전체'라든가 '내가 지정한 경로'로 여러 명에게 쏠 때 사용하는 기본 생성자입니다.
    public ChatEvent(String destination, Object payload) {
        this.targetMemberId = null; // 특정 1명이 아니므로 null
        this.destination = destination; // 내가 적은 경로
        this.payload = payload; // 보낼 데이터
    }
    
    // [생성자 2] '특정 1명(개인)'에게만 1:1로 콕 집어서 알람을 보낼 때 쓰기 편하라고 만든 생성자입니다.
    public ChatEvent(Long targetMemberId, Object payload) {
        this.targetMemberId = targetMemberId; // 보낼 당사자 번호
        this.destination = "/topic/user/" + targetMemberId; // 공통 규칙인 '/topic/user/' 뒤에 당사자 번호를 붙여서 목적지 자동 완성
        this.payload = payload; // 보낼 데이터
    }
}
