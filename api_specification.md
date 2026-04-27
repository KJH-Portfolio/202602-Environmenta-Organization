---
작성일: 2026-04-27T18:45
수정일: 2026-04-27T18:45
---
# 📋 EasyEarth 핵심 비즈니스 API 명세서 (Core API Specification)

> **비동기 파이프라인 및 실시간 통신 중심의 핵심 인터페이스 명세**  
> 이 문서는 파이널 프로젝트의 핵심 비즈니스 로직(채팅, 날씨, AI, 보안)을 수행하는 주요 API 및 소켓 엔드포인트들을 정의하며, 실제 구현된 소스 코드를 통해 설계 의도를 명세합니다.

---

## 📑 목차
1. [💬 전역 STOMP 소켓 연결 및 알림 수신 (Global Notification)](#1-전역-stomp-소켓-연결-및-알림-수신-global-notification)
2. [🛡️ JWT 기반 Stateless 인증 필터링 (Security Guard)](#2-jwt-기반-stateless-인증-필터링-security-guard)
3. [🌤️ AI 비서 멘트 생성 파이프라인 (WebClient & Cache)](#3-ai-비서-멘트-생성-파이프라인-webclient--cache)
4. [📰 글로벌 에코 뉴스 RSS 및 AI 번역 (Data Reliability)](#4-글로벌-에코-뉴스-rss-및-ai-번역-data-reliability)
5. [💎 원자적 채팅 메시지 이벤트 발송 (Transactional Event)](#5-원자적-채팅-메시지-이벤트-발송-transactional-event)

---

## 💬 1. 전역 STOMP 소켓 연결 및 알림 수신 (Global Notification)
- **Endpoint**: `ws://[Domain]/ws-stomp`
- **Technical Point**: 사용자가 특정 채팅방에 있지 않아도 플랫폼 어디서든 알림을 받을 수 있도록 로그인 즉시 전역 채널에 접속합니다.

### 💻 Frontend (React Context) & STOMP Subscribe
```javascript
// ChatContext.jsx 中 - 로그인 완료 시 전역 소켓 연결
const connectGlobalSocket = (userId) => {
    const socket = new SockJS('http://localhost:8080/ws-stomp');
    const stompClient = Stomp.over(socket);

    stompClient.connect({ Authorization: `Bearer ${token}` }, () => {
        // [핵심] 채팅방이 아닌 개인 전용 '전역 알림 채널' 구독
        stompClient.subscribe(`/topic/notifications/${userId}`, (message) => {
            const notification = JSON.parse(message.body);
            updateUnreadBadgeCount(notification); // 어디서든 안 읽음 뱃지 업데이트
        });
    });
};
```

---

## 🛡️ 2. JWT 기반 Stateless 인증 필터링 (Security Guard)
- **Technical Point**: 모든 HTTP API 요청 시 스프링 시큐리티 필터 체인을 통해 토큰의 유효성을 1차적으로 검증합니다.

### 💻 Spring Security - JwtAuthenticationFilter
```java
// JwtAuthenticationFilter.java 中
@Override
protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
    String token = resolveToken(request); // 헤더에서 Bearer 토큰 추출
    
    // [Guard 1] 토큰 유효성 및 만료 여부 검증
    if (token != null && jwtUtil.validateToken(token)) {
        // [Guard 2] 인증 객체 생성 및 SecurityContext 저장
        Authentication auth = jwtUtil.getAuthentication(token);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
    
    // 다음 필터로 정상 요청 인계
    filterChain.doFilter(request, response);
}
```

---

## 🌤️ 3. AI 비서 멘트 생성 파이프라인 (WebClient & Cache)
- **Endpoint**: `GET /gemini/secretary`
- **Technical Point**: 외부 공공데이터(날씨) API 호출과 Gemini AI 호출의 오버헤드를 막기 위해 로컬 파일 캐시(`FileCacheService`)를 활용합니다.

### 💻 Service Logic & File Cache
```java
// GeminiService.java 中
public String generateSecretaryMessage(String weatherData) {
    String cacheKey = "secretary_message_" + LocalDate.now();
    
    // 1. 캐시 히트(Hit) 시 즉시 반환 (API 호출 비용 절감)
    String cachedMessage = fileCacheService.getCache(cacheKey);
    if (cachedMessage != null) return cachedMessage;

    // 2. 캐시 미스(Miss) 시 WebClient를 통해 비동기로 Gemini API 호출
    String prompt = "현재 날씨 데이터(" + weatherData + ")를 바탕으로 친환경 실천 조언을 3문장으로 해줘.";
    String aiResponse = webClient.post()
            .uri("/v1beta/models/gemini-pro:generateContent")
            .bodyValue(createGeminiRequest(prompt))
            .retrieve()
            .bodyToMono(String.class)
            .block(); 
            
    // 3. 캐시에 저장 후 반환
    fileCacheService.saveCache(cacheKey, extractText(aiResponse));
    return extractText(aiResponse);
}
```

---

## 📰 4. 글로벌 에코 뉴스 RSS 및 AI 번역 (Data Reliability)
- **Endpoint**: `GET /global/eco-news`
- **Technical Point**: 국내 API의 데이터 풀(Pool) 한계를 극복하고자 NYT 등 글로벌 RSS를 파싱한 후 Gemini AI로 즉각 한글 번역 요약합니다.

### 💻 RSS Parsing & AI Translation Pipeline
```java
// GlobalEcoNewsService.java 中
public List<NewsDto> getTranslatedEcoNews() {
    // 1. 글로벌 언론사(NYT) 환경 탭 RSS XML 파싱
    List<NewsDto> rawNews = parseRss("https://rss.nytimes.com/services/xml/rss/nyt/Climate.xml");
    
    // 2. Gemini AI를 활용한 실시간 번역 파이프라인
    return rawNews.stream().map(news -> {
        String translatedTitle = geminiService.translateContent(news.getTitle());
        String summarizedContent = geminiService.summarizeContent(news.getDescription());
        news.setTitle(translatedTitle);
        news.setDescription(summarizedContent);
        return news;
    }).collect(Collectors.toList());
}
```

---

## 💎 5. 원자적 채팅 메시지 이벤트 발송 (Transactional Event)
- **Endpoint**: `PUBLISH /app/chat/message`
- **Technical Point**: 채팅 메시지가 DB에 완전히 `COMMIT`된 후에만 알림을 발송하여(데이터 불일치 방지), 실시간 웹소켓 큐를 분리 설계했습니다.

### 💻 Event Listener & Broadcasting
```java
// ChatEventListener.java 中
@Async // 채팅 전송 스레드를 블로킹하지 않도록 비동기 처리
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void handleChatMessageEvent(ChatMessageEvent event) {
    ChatMessageDto message = event.getMessage();
    
    // 1. 채팅방 내부 참여자들에게 브로드캐스트
    messagingTemplate.convertAndSend("/topic/chat/room/" + message.getRoomId(), message);
    
    // 2. 채팅방 밖에 있는 유저들에게 전역 알림 브로드캐스트
    for (String receiverId : getRoomParticipants(message.getRoomId())) {
        if (!receiverId.equals(message.getSenderId())) {
            messagingTemplate.convertAndSend("/topic/notifications/" + receiverId, message);
        }
    }
}
```

---

### 💡 API 설계 공통 원칙
1. **Response Handling**: 모든 클라이언트 요청은 Axios Interceptor에서 통합 처리되며, 401(토큰 만료)/403 에러 시 중앙 집중형 갱신 로직이 수행됩니다.
2. **Exception Handling**: 외부 자원(API, RSS) 파싱 시 타임아웃 예외(`WebClientResponseException`)를 대비하여 기본 캐시(Fallback) 데이터를 반환하도록 방어적으로 설계되었습니다.
3. **Security**: 컨트롤러 진입 전 `JwtAuthenticationFilter`를 통해 인가되지 않은 REST/STOMP 접근을 원천 차단합니다.
