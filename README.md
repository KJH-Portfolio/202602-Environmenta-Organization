---
작성일: 2026-04-27T18:30
수정일: 2026-04-30T17:52
---
# EasyEarth (이지어스) Final Project

> **탄소 발자국 추적과 AI 환경 비서, 실시간 소통을 통해 지속 가능한 생태계를 만드는 친환경 라이프스타일 플랫폼**  
> 이 문서는 파이널 프로젝트의 개요, 핵심 기여도, 그리고 기술적 문제 해결 과정을 담은 통합 대시보드입니다.

---

<br>

<p align="center">
  <img src="https://img.shields.io/badge/Java_17-ED8B00?style=flat-square&logo=openjdk&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring_Boot_3.2-6DB33F?style=flat-square&logo=springboot&logoColor=white" />
  <img src="https://img.shields.io/badge/React_18-61DAFB?style=flat-square&logo=react&logoColor=black" />
  <img src="https://img.shields.io/badge/Oracle_21c_XE-F80000?style=flat-square&logo=oracle&logoColor=white" />
  <img src="https://img.shields.io/badge/Gemini_AI-4285F4?style=flat-square&logo=google&logoColor=white" />
  <img src="https://img.shields.io/badge/Docker-2496ED?style=flat-square&logo=docker&logoColor=white" />
</p>

<div align="center">
  <img src="https://via.placeholder.com/800x450.png?text=EasyEarth+Main+Dashboard" width="90%" alt="메인 화면 미리보기">
</div>

---

> [!IMPORTANT]  
> **실시간 STOMP 통신망과 비동기 AI 파이프라인을 구축하여 플랫폼의 접근성과 정보의 신뢰도를 극대화했습니다.**  
> - 본 문서의 내용은 가독성을 위해 접혀 있습니다. **개인적인 기술 역량 및 구현 로직**은 기여도가 높은 **3, 4, 5번 섹션**에서 중점적으로 확인하실 수 있습니다.

---
<details>
<summary><b>1. 기본 정보 (개발 기간, 기술 스택, 인원 구성) 📅</b></summary>
<br>

- 📅 **개발 기간:** 2026.02 ~ 2026.04 (약 2개월)

- 🖥️ **플랫폼:** Web

- 👥 **개발 인원:** 팀 프로젝트 (본인 기여도: 채팅 전반, 날씨/뉴스 전반, 공통 인증 및 시스템 환경 설정)

- 🛠️ **개발 환경 (Tech Stack):**
  - **Language:** `Java 17`, `JavaScript`, `JSX`
  - **Server/Framework:** `Spring Boot 3.2`, `React 18`
  - **Database & ORM:** `Oracle Database 21c (Docker XE)`, `Spring Data JPA`
  - **Real-time:** `WebSocket`, `STOMP`
  - **AI/External API:** `Google GenAI SDK (Gemma-3)`, `RestTemplate`, `NYT RSS`
  - **Security:** `Spring Security 6`, `JWT`
  - **DevOps:** `Docker`, `Docker Compose`

</details>

---
<details>
<summary><b>2. 프로젝트 전체 개요 (전체 구조, ERD, IA 등) 📊</b></summary>
<br>

**🎯 1. 프로젝트 목표**
> 사용자가 일상에서 환경 보호 활동(대중교통 이용, 다회용기 사용 등)을 즐겁게 실천하고 가시적인 리워드를 얻으며, 이웃과 실시간으로 소통할 수 있는 선순환 환경 커뮤니티 생태계를 구축합니다.

**🗓️ 2. 프로젝트 진행 순서**
- 기획 ➡️ API/DB 설계 ➡️ 환경 설정 및 프레임워크 세팅 ➡️ 개발 ➡️ 테스트 ➡️ 배포

**📊 3. ERD (Entity Relationship Diagram) 및 인프라 설계**
- JWT 인증 기반부터 채팅 세션, AI 캐싱 데이터 보관에 이르는 정교한 데이터베이스 구조도
- 👉 **[전체 데이터 설계도 (erd.md)](erd.md)** | **[인프라 설계 (infrastructure_architecture.md)](infrastructure_architecture.md)**

**🎨 4. 기획 방향성 설계**
- **글로벌 통신 및 비동기 경험:** 사용자가 어느 페이지에 있든 플랫폼 생태계 내의 활동(채팅 알림 등)을 즉각 인지할 수 있는 실시간 반응형 아키텍처 구축.
- **AI 활용과 신뢰도 결합:** 단순 API 호출을 넘어 수집된 원천 데이터를 AI가 가공·번역함으로써 사용자에게 양질의 환경 정보를 제공.

**🗺️ 5. IA (정보 구조도) 및 기술 아키텍처**
- GNB 중심의 서비스 계층 구조와 핵심 비즈니스 로직을 수행하는 API 명세서
- 👉 **[사이트 구조도 (ia.md)](ia.md)** | **[핵심 API 명세서 (api_specification.md)](api_specification.md)**

**🌊 6. User Flow**
- JWT 보안 필터링과 전역 STOMP 소켓 연결 동선 등 핵심 로직의 흐름도
- 👉 **[유저 행동 흐름 (userflow.md) 보러가기](userflow.md)**

**🔑 7. Environment Setup**
- 초기 더미 데이터 현황 및 시스템 설정 현황 명세
- 👉 **[초기 데이터 및 설정 관리 (init_data.md) 보러가기](init_data.md)**

**💡 8. 주요 기능 요약**
- 👤 **회원 및 관리자 기능 (Member & Admin)**
  - JWT 기반 자체 로그인 및 카카오 소셜 로그인, 마이페이지 활동 내역 관리
- 🌱 **탄소 발자국 추적 및 에코 맵 (Map & Carbon Tracking)**
  - 제로 웨이스트 상점 등 서울시 환경 테마 요소를 지도로 탐색 및 맞춤형 길찾기 제공
  - 이동 거리 기반 정밀 탄소 절감량 계산 및 에코 지갑 포인트 적립
  - 나무 성장(EcoTree) 시각화를 통한 게이미피케이션
- 💬 **실시간 커뮤니티 및 채팅 (Community & Chat)**
  - 친환경 실천 기록 공유 게시판 및 사용자 간 실시간 양방향 채팅
  - 플랫폼 내 어디서든 수신 가능한 전역 채팅 알림망 구축
- 🌤️ **AI 환경 비서 및 날씨/뉴스 (Weather, News & AI)**
  - 미세먼지 등 공공 데이터 연동 및 맞춤형 환경 실천 조언(비서 멘트) 생성
  - 신뢰도 높은 글로벌 환경 뉴스 수집 및 Gemini AI 실시간 번역 요약 제공

</details>

---
<details id="core-contributions">
<summary><b>3. 프로젝트 개인 구현 - 백엔드 설계 철학 및 로직 구현 🛠️</b></summary>
<br>

- 🎯 **프로젝트 목표 (Foundation & Integrity):** 
  - **객체 지향적 영속성 관리 및 성능 최적화 (Spring Data JPA):** 단순 SQL 매핑을 넘어 Entity 간 연관 관계 중심의 JPA 아키텍처를 도입하여, 복잡한 실시간 채팅 데이터와 상태 이력을 안전하게 통제하고 `Slice` 페이징 등으로 DB I/O 성능을 최적화했습니다.
  - **외부 API 기반 데이터 파이프라인 및 AI 가공:** 기상청 공공데이터와 글로벌 환경 뉴스(NYT RSS) 등 원시(Raw) 데이터를 서버 단에서 직접 수집하고, 이를 Google Gemini AI와 연동하여 사용자 친화적인 정보(번역, 맞춤형 환경 조언)로 실시간 가공 및 캐싱하는 백엔드 아키텍처를 설계하는 것을 목표로 했습니다.
  - **실시간 비동기 아키텍처 수립:** 클라이언트의 명시적 요청 없이도 서버가 먼저 데이터를 푸시(Push)할 수 있는 **양방향 통신 인프라(WebSocket/STOMP)**를 완벽하게 제어하여 끊김 없는 알림망을 구축했습니다.
  - **확장성 있는 보안 필터 체인 구축:** Session의 한계를 벗어나 **Stateless한 JWT 인증 체계**를 근간으로 삼고, 모든 API와 소켓 핸드쉐이크 단계에서 인증 무결성을 보증하는 방어선을 구축했습니다.

> [!IMPORTANT]
> **Insight: 왜 이 목표를 선정했는가?**  
> 레거시 스택(MyBatis)에서 경험한 단편적 쿼리 매핑의 한계를 극복하고자, 현대적인 **'JPA 기반의 객체 지향적 도메인 설계'**를 시스템의 기초 체력으로 삼았습니다. 이 튼튼한 기반 위에 원천 데이터를 생성형 AI로 가공하는 **'데이터 파이프라인 설계 역량'**과, 서비스의 생동감을 부여하는 **'실시간 통신 및 보안 인프라(STOMP, JWT)'**를 유기적으로 결합했습니다. 즉, 복잡한 기술 스택들이 맞물려 돌아가면서도 데이터 무결성을 유지하고 지연 없는 사용자 경험을 제공하는 고성능 백엔드 시스템을 증명하는 것이 핵심 가치였습니다.

- 📅 **개인 개발 진행 순서 (Sprint):** 
  - `1. 보안/공통 인프라 (2월)` : JWT 인증 필터(`JwtAuthenticationFilter`), SecurityConfig, CORS 설정 등 기본 방어벽 구축
  - `2. 실시간 채팅 파이프라인 (2월말)` : WebSocketConfig 세팅, STOMP 기반 Pub/Sub 라우팅 및 채팅 도메인 완성
  - `3. AI/외부 API 연동 (3월)` : WebClient를 활용한 공공데이터 및 NYT RSS 호출, Gemini AI 서비스 결합
  - `4. 트러블슈팅 고도화 (3월말~4월)` : 전역 STOMP 채널 구독 알고리즘 및 캐시(Cache) 적용, 예외 처리 최적화

- 📊 **개인 구현 ERD (실시간 채팅 코어):**
  > 실시간 소통을 위해 회원과 채팅방 간의 다대다(N:M) 관계를 해소하고, **메시지 이력과 참여 상태를 고속으로 조회할 수 있는 핵심 관계망**을 설계했습니다.
  > *(※ 포트폴리오 가독성을 위해 외래키 조인과 핵심 비즈니스 로직에 관여하는 주요 컬럼만 축약하여 명시했습니다.)*
  > 👉 **[전체 DB 설계도 (erd.md) 보러가기](erd.md)**
  ```mermaid
  erDiagram
      MEMBERS {
          varchar USER_ID PK
          char    ONLINE_STATUS "Y/N 실시간 상태"
      }
      CHAT_MESSAGES {
          number  MSG_ID PK
          number  ROOM_ID FK
          varchar SENDER_ID FK
          varchar MESSAGE_CONTENT
      }
      CHAT_PARTICIPANTS {
          number  PARTICIPANT_ID PK
          number  ROOM_ID FK
          varchar USER_ID FK
      }
      CHAT_ROOMS {
          number  ROOM_ID PK
          varchar ROOM_TITLE
      }

      MEMBERS ||--o{ CHAT_MESSAGES : "메시지 발신"
      MEMBERS ||--o{ CHAT_PARTICIPANTS : "채팅방 참여"
      CHAT_ROOMS ||--o{ CHAT_MESSAGES : "메시지 이력"
      CHAT_ROOMS ||--o{ CHAT_PARTICIPANTS : "참여자 목록"
  ```

- 💡 **기획 방향성 설계 (Core Strategy):** 
  - **전역 통신망 및 알림 분리 설계:** 특정 화면에 종속되지 않는 **Global Notification 채널**을 기획하여 애플리케이션 접속과 동시에 사용자 맞춤형 알림을 즉각 수신할 수 있도록 했습니다.
  - **보안 및 정합성 우선의 설정 제어:** `WebConfig`의 CORS 세팅과 `SecurityConfig`를 명확히 분리하여 보안 계층을 강화하고, `AsyncConfig`를 통해 이벤트 리스너의 성능 지연을 방지했습니다.

#### 핵심 구현 소스 코드 (Core Implementation)
> 실시간 데이터 스트리밍과 보안, 외부 AI 연동을 직접 설계하고 구현한 핵심 파일들입니다.

- **Security & Config**: `JwtAuthenticationFilter.java` / `SecurityConfig.java` / `WebSocketConfig.java`
- **Chat Domain**: `ChatController.java` / `ChatServiceImpl.java` / `ChatEventListener.java`
- **Weather & AI**: `WeatherService.java` / `GlobalEcoNewsService.java` / `GeminiService.java`
- **Frontend Interceptor**: `apis/axios.jsx` (JWT 토큰 자동 첨부 및 예외 처리)

#### 주요 기능 하이라이트 (Functional Highlights)

**1) 전역 알림 기반 실시간 채팅 커뮤니티**
- 어디서든 수신 가능한 전역 STOMP 채널을 통해 끊김 없는 알림을 제공하며, 사용자 간 실시간 양방향 소통이 가능한 채팅방을 지원합니다.
<br>
<img src="./source/채팅_메인.png" width="100%" alt="실시간 채팅 및 전역 알림 화면">


**2) 맞춤형 날씨 정보 및 AI 환경 비서**
- 기상청 실시간 데이터와 Google Gemini AI를 연동하여, 현재 날씨와 맥락에 맞는 친환경 실천 멘트를 대시보드에서 즉각 제공합니다.
<br>
<img src="./source/날씨_비서.png" width="100%" alt="날씨 정보 및 AI 환경 비서 화면">


**3) 글로벌 환경 뉴스 수집 및 AI 요약 번역**
- NYT 등 해외 유력 언론사의 환경 섹션 기사를 실시간 수집하고, AI를 통해 빠르고 정확하게 한글로 번역 및 요약하여 제공합니다.
<br>
<img src="./source/환경_뉴스.png" width="100%" alt="글로벌 환경 뉴스 화면">

</details>

---
<details id="technical-deepdive">
<summary><b>4. 기술적 깊이 - 까다로운 문제 해결 및 성능 최적화 사례 🚀</b></summary>
<br>

**🔍 핵심 로직 분석 (Core Logic Analysis)**

**1️⃣ [Data Architecture] JPA 시스템 도입**
- **구조 설계:** 기존 레거시 스택(MyBatis)의 단편적인 쿼리 매핑에서 벗어나, 데이터베이스 테이블을 객체 중심으로 다루어 유지보수성을 높이고 비즈니스 로직에 집중하고자 했습니다.
- **기능 구현:** `Spring Data JPA`를 도입하여 복잡한 실시간 채팅 내역과 알림 상태를 Entity간 연관 관계로 매핑했습니다. 대용량 데이터 로딩 방지를 위해 `Slice` 객체와 커서(Cursor) 기반 페이징을 구현하여 서버 부하를 최소화하는 데이터 접근 계층을 완성했습니다.

```java
// ChatMessageEntity.java 中 (JPA Entity 기본 매핑)
@Entity
@Table(name = "CHAT_MESSAGES")
public class ChatMessageEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long chatRoomId;
    
    @Column(nullable = false)
    private String messageContent;
}

// ChatMessageRepository.java 中 (커서 기반 Slice 페이징 구현)
@Query("SELECT m FROM ChatMessageEntity m WHERE m.chatRoomId = :roomId AND m.id < :cursorId ORDER BY m.id DESC")
Slice<ChatMessageEntity> findMessagesByCursor(@Param("roomId") Long roomId, @Param("cursorId") Long cursorId, Pageable pageable);
```

**2️⃣ [Real-time] STOMP 구독/발행 시스템**
- **구조 설계:** 단순한 단방향 HTTP 요청을 넘어, 채팅과 같은 다수 사용자의 동시 다발적인 상호작용을 지연 없이 매끄럽게 처리하는 양방향 통신 인프라를 구축하고자 했습니다.
- **기능 구현:** `WebSocketConfig`를 통해 엔드포인트를 개방하고 STOMP 프로토콜을 도입하여 메시지 브로커가 `/topic` 및 `/queue` 경로로 메시지를 중개하도록 라우팅을 설계했습니다. `@TransactionalEventListener`를 통해 DB 저장과 메시지 브로드캐스트의 원자적 트랜잭션을 보장했습니다.

```java
// WebSocketConfig.java 中 (STOMP 메시지 브로커 설정)
@Override
public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry.enableSimpleBroker("/topic", "/queue"); // 구독(Sub) 경로
    registry.setApplicationDestinationPrefixes("/app"); // 발행(Pub) 경로
}
```

**3️⃣ [UX / Architecture] 전역 채널 구독**
- **구조 설계:** 채팅방 밖에서도 알림을 즉시 받아볼 수 있도록 하여, 플랫폼 내 어디서든 끊김 없는 사용자 경험(UX)과 완벽한 비동기 알림 생태계를 제공하고자 했습니다.
- **기능 구현:** 프론트엔드의 최상단 컴포넌트에서 로그인된 유저 ID 기반의 전역 채널(`/queue/user/{userId}`)을 자동 구독(Subscribe)하도록 설계하고, 백엔드에서는 새로운 알림 이벤트 발생 시 수신자의 전역 채널로 즉시 푸시(Push)하도록 통신망을 구축했습니다.

```javascript
// App.jsx 최상단 (전역 소켓 구독 설정)
stompClient.subscribe(`/queue/user/${user.userId}`, (message) => {
    const alertData = JSON.parse(message.body);
    updateGlobalBadgeCount(alertData); // 안 읽음 뱃지 즉각 갱신
});
```

**4️⃣ [AI Pipeline] 환경 뉴스 연동 및 Gemini 번역/요약 파이프라인**
- **구조 설계:** 한정된 국내 환경 데이터의 제약을 극복하고, 신뢰도 높은 글로벌 인사이트를 사용자 친화적인 한국어로 제공하여 플랫폼의 부가가치를 높이고자 했습니다.
- **기능 구현:** 해외 언론사(NYT 등)의 환경 섹션 RSS 피드를 정기적으로 수집하고, `Google Gemini API`에 영문 기사 데이터를 프롬프팅하여 실시간으로 한글 번역 및 핵심 요약을 생성하는 완전 자동화 파이프라인을 구축했습니다.

```java
// GeminiService.java 中 (Gemma-3를 활용한 기사 번역/요약)
String prompt = "다음 영문 환경 기사를 한국어로 요약해줘: " + articleText;
GenerateContentResponse response = client.models.generateContent("gemma-3-27b-it", prompt, null);
return response.text();
```

**5️⃣ [Security] Axios 인터셉터 토큰 자동 초기화**
- **구조 설계:** 토큰 만료 시 서버에서 던지는 401 에러를 일일이 처리하는 대신 전역적으로 가로채어 사용자에게 불편한 에러 창이 연속해서 뜨는 것을 방지하고 매끄러운 인증 관리를 구현하고자 했습니다.
- **기능 구현:** 프론트엔드의 `Axios Interceptor` 응답 계층에서 서버의 상태 코드를 감지하고, `401 Unauthorized` 발생 시 로컬 스토리지의 토큰을 즉각 파기하며 로그인 페이지로 자연스럽게 유도하는 자동화 흐름을 완성했습니다.

```javascript
// axios.jsx 中 (JWT 토큰 401 검증 실패 시 자동 초기화)
api.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401) {
            localStorage.removeItem('token'); // 토큰 파기
            window.location.href = '/login'; // 로그인 페이지 유도
        }
        return Promise.reject(error);
    }
);
```

---

> [!TIP]
> **더 상세한 기술적 페인 포인트와 해결 과정은 [Troubleshooting Deep Dive](./troubleshooting_deep_dive.md) 문서에서 확인하실 수 있습니다.**

**🤔 1. Decision Making (Technical Rationale)**

- **Spring Data JPA (MyBatis vs JPA):** 
  - **불필요한 반복 작업 제거 및 유지보수성 향상:** 이전 프로젝트에서 MyBatis를 사용하며 아주 간단한 조회 쿼리조차 매번 매퍼 파일에 직접 작성해야 하는 번거로움을 경험했습니다. 특히 프로젝트 규모가 커짐에 따라 매퍼 코드가 방대해졌고, 새로운 DB 접근 로직을 추가할 때마다 기존 매퍼 구문들 중 중복이 있는지 일일이 확인해야 하는 등 유지보수 효율이 급격히 저하되는 문제를 겪었습니다. 이를 해결하기 위해 이번 프로젝트에서는 엔티티 기반으로 쿼리를 자동화하고 객체 지향적으로 데이터를 관리할 수 있는 JPA를 도입했습니다.
- **WebSocket & STOMP (Legacy WebSocket vs STOMP):**
  - **구조적 한계 극복 및 확장성 확보:** 기존에 사용하던 레거시 웹소켓 방식은 채팅방이 늘어나거나 기능이 추가될 때마다 그에 대응하는 핸들러 클래스를 매번 제작해야 하는 한계가 있었습니다. 이는 곧 코드 양의 폭발적인 증가와 유지보수의 어려움으로 이어졌습니다. 반면 STOMP는 하나의 엔드포인트 내에서 메시지 브로커를 통해 경로(Topic/Queue)별로 메시지를 유연하게 라우팅할 수 있어, 클래스 증설 없이도 다수의 채팅방과 전역 알림 기능을 체계적으로 관리할 수 있다는 확신을 가지고 도입을 결정했습니다.
- **Gemini AI (AI Convergence):**
  - **정보의 신뢰도와 가시성을 고려한 AI 데이터 가공:** 외부 뉴스 데이터를 그대로 노출할 경우 홈페이지 대시보드에 불필요한 정보가 너무 많이 포함되는 문제가 있었고, 반대로 AI에게만 의존하여 내용을 생성할 경우 정보의 신뢰도가 매우 낮아지는 한계가 있었습니다. 이를 해결하기 위해 신뢰할 수 있는 실질적인 데이터(RSS 등)를 서버에서 먼저 확보하고, 이를 AI가 가독성 있게 요약 및 번역하게 함으로써 정보의 정확성과 전달력을 동시에 확보했습니다.

**🔥 2. Troubleshooting: 문제 해결 및 설계적 방어 사례 (Key Highlights)**

> [!TIP]
> **모든 사례에 대한 [상세 기술 분석 및 코드 비교 보고서](./troubleshooting_deep_dive.md)가 별도로 준비되어 있습니다.**

1. **[Security] Axios 인터셉터 보안 가드:** 401/403 예외의 전역 처리를 통해 개별 컴포넌트의 인증 예외 처리 중복 코드를 100% 제거하고 보안 흐름을 단일화했습니다.
2. **[Real-time] 전역 알림 라우팅 설계:** 특정 채팅방에 국한되었던 소켓 구독 아키텍처를 전역 Context로 확장하여, 채팅방 이탈 시에도 알림 수신율을 0%에서 100%로 보장했습니다.
3. **[Performance] 서버 사이드 파일 캐싱:** 외부 환경 뉴스 API의 동기 통신 지연(평균 3,000ms)을 로컬 I/O 기반 비동기 캐시로 전환하여 응답 시간을 10ms 이하로 단축했습니다.
4. **[Data] AI 데이터 파이프라인 구축:** 단순 RSS 직노출 방식을 탈피하고 Gemini API 파이프라인을 구축하여, 불필요한 원천 데이터 노이즈를 제거하고 가독성 높은 한글 요약을 100% 자동 생성했습니다.
5. **[UX/DB] 무한 스크롤 & JPA Slice 최적화:** Offset 방식의 한계인 Count 쿼리 오버헤드를 Slice 기반 커서 페이징으로 대체하여, 10만 건 이상의 대용량 테이블에서도 O(1) 수준의 일관된 조회 속도를 확보했습니다.
6. **[Frontend UX] 역방향 스크롤 점핑 제어:** 과거 채팅 메시지 로드 시 스크롤이 위로 튀는 현상을 `scrollHeight` 차분 계산과 DOM 렌더링 마이크로 딜레이를 통해 제어하여 완벽한 시선 고정을 달성했습니다.
7. **[Concurrency] 실시간 채팅 동시성 제어:** 초당 다수의 메시지 발생 시 채팅방 마지막 메시지가 누락되는 Race Condition을 낙관적 락(Optimistic Lock)과 Exponential Backoff 재시도 로직으로 방어했습니다.

</details>

---
<details id="retrospective-growth">
<summary><b>5. 회고 - 프로젝트 성찰 및 향후 기술적 지향점 📈</b></summary>
<br>

- **🟢 Keep (Continuous Learning): 새로운 기술 습득을 통한 구현 역량의 확장**
  - 이번 프로젝트에서 **JPA와 STOMP**를 새롭게 학습하고 적용하며, 기존 레거시(MyBatis/순수 WebSocket) 방식보다 훨씬 방대한 기능을 더 효율적이고 편리하게 구현할 수 있음을 체감했습니다. 
  - 앞으로도 새로운 기술과 툴을 적극적으로 탐색하고 도입하여, 프로젝트의 퀄리티를 지속적으로 고도화하는 '성장형 개발자'로서의 태도를 견지하고자 합니다.

- **🔴 Problem (Security Awareness): 초기 설정 및 보안 관리의 미흡함**
  - 프로젝트 초기 단계에서 API 키나 환경 변수 관리 등 보안적인 측면에 소홀했던 점을 반성합니다. 특히 '기능 구현'에만 매몰되어 **Gemini API 키를 코드에 그대로 포함하여 노출**시키는 실수를 범하기도 했습니다.
  - 보안이 조금 미흡하더라도 당장의 실행에는 문제가 없다는 안일함이 기술적 부채로 이어질 수 있음을 뼈저리게 느꼈습니다.

- **🔵 Try (Security-First Planning): 환경 변수(.env) 체계 도입 및 보안 고도화**
  - 모든 API 키와 DB 접속 정보를 `.env`로 분리하고 리팩토링하여 보안성과 유지보수성을 확보했습니다.
  - 향후 기획 단계부터 보안 프로토콜을 우선 설계하는 'Security-First' 접근법을 체득하는 계기가 되었습니다.

</details>

---
<details>
<summary>부록: 프로젝트 실행 방법 (Docker)</summary>

> 이 프로젝트는 **Docker** 환경이 모두 세팅되어 있어 매우 간편하게 실행할 수 있습니다.

1. **사전 준비**: 로컬 환경에 `Docker Desktop` 설치 및 실행
2. **프로젝트 클론 및 설정**
   ```bash
   git clone [레포지토리 주소]
   cd 2026-bootcamp_final_project
   
   # .env.example 파일을 복사하여 .env 파일을 만들고 실제 API 키를 입력하세요.
   # [중요] DB 접속 주소는 jdbc:oracle:thin:@db:1521/XEPDB1 형식을 사용합니다.
   cp .env.example .env 
   ```
3. **Docker Compose를 통한 컨테이너 빌드 및 백그라운드 실행**
   ```bash
   docker-compose up -d --build
   ```
4. **접속하기**
   - **Frontend**: `http://localhost:8080`
   - **Backend (API Docs)**: `http://localhost:8081/spring/swagger-ui/index.html`
   - **Database**: `XEPDB1` (Host Port: 1523)

</details>
