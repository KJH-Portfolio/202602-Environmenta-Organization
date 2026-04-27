---
작성일: 2026-04-27T18:50
수정일: 2026-04-27T18:50
---
# 🌊 EasyEarth 사용자 비즈니스 동선 정의 (User Flow)

> **사용자 경험(UX) 중심의 기능 프로세스 및 예외 처리 아키텍처**  
> 이 문서는 실시간 채팅 참여, 환경 데이터(날씨/뉴스) 조회, AI 비서 활용 등 플랫폼의 핵심 비즈니스 로직별 사용자 이동 경로와 시스템 자동화 프로세스를 다이어그램을 통해 정의합니다.

---

## 📑 목차
1. [역할별 권한 플로우](#1-역할별-권한-플로우)
2. [핵심 기능별 상세 플로우 (채팅 / 환경 비서)](#2-핵심-기능별-상세-플로우)
3. [시스템 자동화 로직 (Automation)](#3-시스템-자동화-로직-automation)
4. [통합 비즈니스 플로우 (Full Flow)](#4-통합-비즈니스-플로우-full-flow)

---

## 1. 역할별 권한 플로우

### 👁️ 비로그인 사용자 (Public)

```mermaid
flowchart TD
    START([사이트 접속]) --> MAIN[메인 페이지]

    MAIN --> M1[날씨 및 AI 비서 조회]
    MAIN --> M2[글로벌 환경 뉴스 조회]
    MAIN --> M3[커뮤니티 글 목록 조회]
    MAIN --> M4[로그인 / 카카오 연동]

    %% 채팅
    MAIN --> C1{실시간 채팅방 입장 시도}
    C1 -->|"🔒 로그인 필요"| LOGIN[로그인 모달 / 페이지]

    %% 커뮤니티
    M3 --> C2[게시글 상세 조회]
    C2 --> C3{글 작성/댓글 시도}
    C3 -->|"🔒 로그인 필요"| LOGIN

    %% 로그인 이후
    LOGIN -->|성공| LOGGED[로그인 완료 → JWT 토큰 발급]
    LOGIN -->|실패| FAIL[인증 실패 안내]
    FAIL --> LOGIN

    style START fill:#4CAF50,color:#fff
    style LOGIN fill:#FF9800,color:#fff
```

### 👤 일반 사용자 (MEMBER)

```mermaid
flowchart TD
    START([로그인 및 JWT 획득]) --> EVENT[전역 STOMP 채널 자동 구독]
    EVENT --> MAIN[메인 페이지]

    MAIN --> M1["💬 실시간 채팅"]
    MAIN --> M2["🌤️ 날씨 / 뉴스 / AI 비서"]
    MAIN --> M3["🌱 탄소 발자국 추적"]
    MAIN --> M4["📝 에코 커뮤니티"]

    %% ── 채팅 ──
    M1 --> C1[채팅방 목록]
    C1 --> C2[새 방 개설]
    C1 --> C3[기존 방 입장]
    C3 --> C4[실시간 채팅 전송]
    C4 --> C5[상대방 뱃지 알림 갱신]

    %% ── 날씨 / 뉴스 / AI ──
    M2 --> W1[현재 위치 기반 날씨 조회]
    M2 --> N1[글로벌 뉴스 AI 번역 조회]
    W1 --> A1[AI 비서 맞춤형 멘트 생성]

    %% ── 탄소 추적 ──
    M3 --> T1[이동 경로/수단 입력]
    T1 --> T2[탄소 절감량 계산]
    T2 --> T3[지갑 포인트 적립 & 에코트리 성장]

    style START fill:#4CAF50,color:#fff
    style EVENT fill:#FF9800,color:#fff
```

---

## 2. 핵심 기능별 상세 플로우

### 💬 2.1 실시간 채팅 및 알림 플로우 (WebSocket/STOMP)

```mermaid
flowchart LR
    S([로그인]) --> SUB["전역 알림 채널 구독<br/>(/topic/notifications/myId)"]
    SUB --> MAIN[플랫폼 자유 이용]
    
    OTHER["타 사용자가<br/>나에게 메시지 발송"] --> EVENT["@TransactionalEventListener<br/>메시지 비동기 처리"]
    EVENT --> PUSH["전역 채널로 메시지 Push"]
    PUSH --> ALARM{"내 현재 화면 위치"}
    ALARM -->|해당 채팅방 내부| CHAT["메시지 실시간 렌더링"]
    ALARM -->|다른 페이지| BADGE["GNB 안 읽음 뱃지 카운트 +1"]

    style S fill:#4CAF50,color:#fff
    style EVENT fill:#F44336,color:#fff
    style PUSH fill:#2196F3,color:#fff
```

### 🌤️ 2.2 AI 비서 및 글로벌 뉴스 파이프라인

```mermaid
flowchart LR
    S([사용자 요청]) --> REQ{"FileCache 조회"}
    REQ -->|Cache Hit| RES["캐시 데이터 즉시 반환<br/>(0.1초 이내)"]
    REQ -->|Cache Miss| API["WebClient 비동기 호출"]
    
    API -->|날씨 데이터| W_API["기상청 공공데이터 파싱"]
    API -->|뉴스 데이터| N_API["NYT RSS 수집"]
    
    W_API --> GEMINI["Google Gemini AI 연동"]
    N_API --> GEMINI
    
    GEMINI -->|조언 생성 / 번역 요약| SAVE["FileCache 결과 저장"]
    SAVE --> RES

    style S fill:#4CAF50,color:#fff
    style REQ fill:#FF9800,color:#fff
    style GEMINI fill:#03A9F4,color:#fff
```

---

## 3. 시스템 자동화 로직 (Automation)

프로젝트의 운영 효율성을 높이고 외부 통신 지연을 방지하기 위해 백엔드에서 자동으로 수행되는 로직입니다.

### ⏰ 3.1 파일 캐시 자동 갱신 (DataScheduler)
외부 API의 쿼터 제한을 피하고 즉각적인 응답 속도를 제공하기 위해 서버 스케줄러가 백그라운드에서 데이터를 수집 및 가공합니다.

```mermaid
flowchart LR
    SCHEDULER([Cron: 매 정각 / 특정 시간대]) --> TASK1[날씨 데이터 갱신]
    SCHEDULER --> TASK2[글로벌 뉴스 갱신]
    
    TASK1 --> GEMINI[Gemini AI 가공/번역]
    TASK2 --> GEMINI
    
    GEMINI --> UPDATE[로컬 캐시 파일 Overwrite]
    
    style SCHEDULER fill:#FF9800,color:#fff
```

---

## 4. 통합 비즈니스 플로우 (Full Flow)

```mermaid
flowchart TD
    S([사이트 접속]) --> MAIN[🏠 메인 페이지]

    MAIN --> AUTH{로그인 상태 검증<br/>(JWT)}

    AUTH -->|"❌ 비로그인 (Public)"| GUEST["조회 전용<br/>(날씨/뉴스/게시글)"]
    AUTH -->|"✅ 인가됨 (Private)"| USER[전체 기능 이용 가능]

    %% 비로그인 → 로그인 유도
    GUEST -->|채팅/탄소기록 등 접근| LOGIN[로그인/카카오 연동]
    LOGIN -->|성공| USER

    %% 일반 사용자 기능
    USER --> U1["💬 실시간 채팅망 접속"]
    USER --> U2["🌤️ AI 환경 비서 소통"]
    USER --> U3["🌱 탄소 절감 활동"]
    USER --> U4["📋 에코 지갑/마이페이지"]

    U1 --> RESULT1["양방향 소통 및 알림 수신"]
    U2 --> RESULT2["환경 뉴스 및 조언 획득"]
    U3 --> RESULT3["포인트 획득 및 나무 성장"]

    style S fill:#4CAF50,color:#fff
    style MAIN fill:#2196F3,color:#fff
    style AUTH fill:#FF9800,color:#fff
    style GUEST fill:#9E9E9E,color:#fff
```
