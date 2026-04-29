---
작성일: 2026-04-27T18:45
수정일: 2026-04-28T16:50
---
# EasyEarth 핵심 비즈니스 API 명세서 (Core API Specification)

> **보안 아키텍처, 프로토콜 설계, 인터페이스 명세 중심의 시스템 기술서**  
> 이 문서는 파이널 프로젝트의 핵심 비즈니스 로직을 수행하는 주요 API 및 소켓 엔드포인트들을 계층별로 정의하며, 실제 구현된 소스 코드를 통해 설계 의도를 명세합니다.

---

### 인증 및 보안 아키텍처]
1. [🛡️ 통합 핸드쉐이크 및 JWT 보안 전략 (Unified Handshake)](#1-통합-핸드쉐이크-및-jwt-보안-전략-unified-handshake)

### 통신 프로토콜 설계]
2. [💬 전역 알림 수신 채널 (Global Notification)](#2-전역-알림-수신-채널-global-notification)
3. [💎 원자적 메시지 이벤트 브로드캐스팅 (Transactional Event)](#3-원자적-메시지-이벤트-브로드캐스팅-transactional-event)

### 인터페이스 설계]
4. [🌤️ AI 비서 조언 파이프라인 (AI Secretary)](#4-ai-비서-조언-파이프라인-ai-secretary)
5. [📰 글로벌 에코 뉴스 배치 프로세스 (Eco News Batch)](#5-글로벌-에코-뉴스-배치-프로세스-eco-news-batch)
6. [🌳 에코트리 성장 및 단계별 진화 (EcoTree Gamification)](#6-에코트리-성장-및-단계별-진화-ecotree-gamification)
7. [✍️ 에코 액티비티 통합 보상 파이프라인 (Integrated Reward)](#7-에코-액티비티-통합-보상-파이프라인-integrated-reward)

---

## 1. 통합 핸드쉐이크 및 JWT 보안 전략 (Unified Handshake)
- **Technical Point**: HTTP(REST)와 WebSocket(STOMP) 등 서로 다른 프로토콜이 혼재된 환경에서 JWT를 기반으로 단일화된 보안 게이트웨이를 구축했습니다.

### Frontend: Axios & SockJS Handshake
```javascript
// 1. HTTP API 인터셉터 (axios.jsx)
api.interceptors.request.use((config) => {
    const token = localStorage.getItem('token');
    if (token) config.headers.Authorization = `Bearer ${token}`; // 모든 요청에 토큰 주입
    return config;
});

// 2. WebSocket 연결 및 핸드쉐이크 (ChatContext.jsx)
const socket = new SockJS('http://localhost:8080/ws-stomp');
const stompClient = Stomp.over(socket);

// CONNECT 프레임 헤더에 토큰을 실어 전송하여 핸드쉐이크 단계에서 인증 수행
stompClient.connect({ Authorization: `Bearer ${token}` }, () => {
    console.log("WebSocket Handshake Success!");
});
```

### Backend: Security Guard (JwtFilter)
```java
// JwtFilter.java 中 - 모든 요청(Handshake 포함)의 관문
@Override
protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
    // 1. Authorization 헤더에서 Bearer 토큰 추출
    String authHeader = request.getHeader("Authorization");
    
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
        String token = authHeader.substring(7);
        
        // 2. JWT 유효성 검증 (위변조/만료 확인)
        if (jwtUtil.validateToken(token)) {
            String loginId = jwtUtil.getUserIdFromToken(token);
            
            // 3. 인증 객체 생성 및 시큐리티 컨텍스트 주입 (Spring Security 인가 완료)
            Authentication auth = new UsernamePasswordAuthenticationToken(loginId, null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
    }
    filterChain.doFilter(request, response);
}
```

---

## 2. 전역 알림 수신 채널 (Global Notification)
- **Endpoint**: `ws://[Domain]/ws-stomp`
- **Technical Point**: 사용자가 특정 채팅방에 있지 않아도 플랫폼 어디서든 실시간 알림을 받을 수 있도록 로그인 즉시 전역 채널을 구독합니다.

```javascript
// ChatContext.jsx 中 - 전역 알림 구독 로직
const connectGlobalSocket = (userId) => {
    const socket = new SockJS('http://localhost:8080/ws-stomp');
    const stompClient = Stomp.over(socket);

    stompClient.connect({ Authorization: `Bearer ${token}` }, () => {
        // [핵심] 개인 전용 '전역 알림 채널' 구독하여 플랫폼 전역 메시지 수신
        stompClient.subscribe(`/topic/notifications/${userId}`, (message) => {
            const notification = JSON.parse(message.body);
            updateUnreadBadgeCount(notification); // 안 읽음 뱃지 실시간 동기화
        });
    });
};
```

---

## 3. 원자적 메시지 이벤트 브로드캐스팅 (Transactional Event)
- **Technical Point**: 데이터 정합성을 위해 메시지가 DB에 **COMMIT**된 직후에만 알림을 발송하도록 `TransactionalEventListener`를 활용했습니다.

```java
// ChatEventListener.java 中 - 커밋 후 알림 발송
@Async // 채팅 전송 스레드를 블로킹하지 않도록 비동기 처리
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void handleChatMessageEvent(ChatMessageEvent event) {
    ChatMessageDto message = event.getMessage();
    
    // 1. 해당 채팅방 내부 참여자들에게 실시간 브로드캐스트
    messagingTemplate.convertAndSend("/topic/chat/room/" + message.getRoomId(), message);
    
    // 2. 채팅방 밖에 있는 참여자들에게 전역 알림 브로드캐스트 (Badge 갱신용)
    for (String receiverId : getRoomParticipants(message.getRoomId())) {
        if (!receiverId.equals(message.getSenderId())) {
            messagingTemplate.convertAndSend("/topic/notifications/" + receiverId, message);
        }
    }
}
```

---

## 4. AI 비서 조언 파이프라인 (AI Secretary)
- **Technical Point**: 인프라 비용 절감 및 성능을 위해 **파일 시스템 기반 캐싱**을 활용하며, 캐시 미스 시에만 Gemini SDK를 통해 맞춤형 멘트를 생성합니다.

```java
// GeminiService.java 中 - SDK 호출 및 캐싱 로직
public String refreshSecretaryAdvice(Map<String, Object> weatherData) {
    // 1. 기상 데이터 기반 맞춤형 친환경 프롬프트 구성
    String prompt = "날씨(" + weatherData + ")에 맞는 환경 실천 활동을 3문장으로 추천해줘.";
    
    // 2. Gemini SDK를 활용한 블로킹 호출 (Servlet 환경 통합)
    try {
        GenerateContentResponse response = client.models.generateContent("gemini-pro", prompt, null);
        String result = response.text();
        
        // 3. 생성된 멘트를 로컬 파일에 캐싱 (I/O 활용으로 외부 인프라 의존성 제거)
        if (result != null) fileCacheService.save("secretary_advice.json", result);
        return result;
    } catch (Exception e) {
        return "fallback message";
    }
}
```

---

## 5. 글로벌 에코 뉴스 배치 프로세스 (Eco News Batch)
- **Technical Point**: NYT RSS를 파싱하여 Gemini AI로 가공합니다. API Rate Limit 방지를 위해 **`@Scheduled` 기반 배치**로 설계되었습니다.

```java
// GlobalEcoNewsService.java 中 - RSS 파싱 및 AI 가공 루프
private String fetchGlobalNewsFromApi() {
    for (int i = 0; i < RSS_URLS.size(); i++) {
        // 1. RSS XML 파싱 및 상위 5개 기사 추출
        NodeList itemList = doc.getElementsByTagName("item");
        for (int j = 0; j < itemList.getLength() && count < 5; j++) {
            categoryNewsBuilder.append(String.format("Title: %s\nDesc: %s\n...", title, desc));
        }
        
        // 2. 카테고리별 뉴스 텍스트를 통째로 Gemini에게 전달하여 번역/요약 요청
        String categoryJsonArray = geminiService.getNewsByCategory(category, categoryNewsBuilder.toString());
        finalJsonBuilder.append("\"").append(category).append("\": ").append(categoryJsonArray);
    }
    return finalJsonBuilder.toString();
}
```

---

## 6. 에코트리 성장 및 단계별 진화 (EcoTree Gamification)
- **Technical Point**: 누적 포인트를 경험치로 환산하여 나무를 4단계로 진화시킵니다. 6개월 완주 목표의 임계치를 설정했습니다.

```java
// EcoTreeService.java 中 - 레벨링 임계치 로직
private static final long LV2_THRESHOLD = 500000L;    // 약 24일차
private static final long LV3_THRESHOLD = 1500000L;   // 약 71일차
private static final long LV4_THRESHOLD = 3780000L;   // 6개월 완주

@Transactional
public EcoTreeVO growTree(int memberId) {
    long currentTotalEarned = tree.getTotalEarnedPoint();
    tree.setSyncedExp(currentTotalEarned); // 포인트 -> 경험치 동기화
    
    // 경험치 기반 레벨 계산 및 원자적 업데이트
    int newLevel = calculateLevel(currentTotalEarned);
    tree.setTreeLevel(newLevel);
    ecoTreeMapper.updateTreeGrowth(tree);
    return tree;
}
```

---

## 7. 에코 액티비티 통합 보상 파이프라인 (Integrated Reward)
- **Technical Point**: 모든 활동 상태를 `INTEGRATED_HISTORY`로 관리하며, 정답 시에만 원자적으로 보상을 지급합니다.

```java
// QuizService.java 中 - 활동 이력 및 포인트 통합 처리
@Transactional
public void saveQuizAttempt(int userId, int quizNo, boolean isCorrect, int point) {
    // 1. 통합 활동 이력(INTEGRATED_HISTORY) 상태 업데이트 (정답여부 Y/N)
    historyMapper.updateHistoryStatus(userId, "QUIZ", quizNo, isCorrect ? "Y" : "N", null);

    // 2. 정답일 경우에만 원자적으로 지갑 포인트 지급 (Transaction 보장)
    if (isCorrect && point > 0) {
        quizMapper.updateMemberPoints(userId, point);
    }
}
```

---

### API 설계 공통 원칙
1. **Performance**: 외부 자원 호출 시 반드시 캐시(File Cache) 전략을 사용하여 외부 의존성과 인프라 비용을 최소화합니다.
2. **Reliability**: 데이터가 DB에 완전히 반영(Commit)된 후에만 사용자에게 알림을 발송하여 데이터 불일치를 원천 차단합니다.
3. **Efficiency**: Redis 등 고비용 인프라 대신 파일 시스템과 스케줄러를 조합하여 가용성과 경제성을 동시에 확보했습니다.
