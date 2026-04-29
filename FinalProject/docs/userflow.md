# EasyEarth 프로젝트 User Flow

> **사용자 행동 경로 및 비즈니스 로직 다이어그램**  
> Mermaid `flowchart` 문법을 활용하여 서비스의 핵심 기능별 프로세스와 예외 처리 흐름을 정의합니다.

---

## 목차

1. [역할별 권한 플로우](#1-역할별-권한-플로우)
   - [비로그인 사용자 (Guest)](#비로그인-사용자-guest)
   - [일반 사용자 (Member)](#일반-사용자-member)
2. [핵심 기능별 상세 플로우](#2-핵심-기능별-상세-플로우)
   - [🌏 에코 맵 및 탄소 산출 플로우](#에코-맵-및-탄소-산출-플로우)
   - [💬 실시간 채팅 초대/참여 플로우](#실시간-채팅-초대참여-플로우-asymmetric-process)
   - [📝 커뮤니티 거버넌스 (신고/블라인드)](#커뮤니티-거버넌스-신고블라인드-로직)
3. [통합 서비스 라이프사이클](#3-통합-서비스-라이프사이클)

---

## 1. 역할별 권한 플로우

### 비로그인 사용자 (Guest)

```mermaid
flowchart TD
    START([사이트 접속]) --> MAIN[메인 대시보드]

    MAIN --> M1[날씨/뉴스/AI 조언 조회]
    MAIN --> M2[에코 맵 상점/경로 조회]
    MAIN --> M3[커뮤니티 게시글 조회]
    MAIN --> M4[로그인 / 회원가입 페이지]

    %% 제약 사항
    M2 -->|"🔒 활동 시도"| LOGIN[로그인 유도]
    M3 -->|"🔒 글쓰기/댓글 시도"| LOGIN
    
    LOGIN -->|성공| LOGGED[인증 토큰 발급 → 메인 리다이렉트]
    LOGIN -->|실패| FAIL[에러 메시지 출력]
    FAIL --> LOGIN

    style START fill:#4CAF50,color:#fff
    style LOGIN fill:#FF9800,color:#fff
    style LOGGED fill:#2196F3,color:#fff
```

---

### 일반 사용자 (Member)

```mermaid
flowchart TD
    START([로그인 완료]) --> MAIN[개인화 대시보드]

    MAIN --> U1["🌏 에코 맵 활동"]
    MAIN --> U2["💬 실시간 채팅"]
    MAIN --> U3["📝 커뮤니티"]
    MAIN --> U4["🌱 마이페이지 (에코트리)"]

    %% 에코 맵
    U1 --> U1A[상점 리뷰 작성]
    U1 --> U1B[탄소 절감 경로 계산]
    U1B --> U1C{이동 완료 인증}
    U1C -->|성공| U1D["💰 포인트 지급 (Atomic Transaction)"]

    %% 채팅
    U2 --> U2A[채팅방 생성/참여]
    U2A --> U2B[실시간 메시징]
    U2B --> U2C[상대방 초대/강퇴]

    %% 마이페이지
    U4 --> U4A[출석/퀴즈 참여]
    U4A --> U4B[XP 획득 ➡️ 나무 성장]

    style START fill:#4CAF50,color:#fff
    style U1D fill:#388E3C,color:#fff
    style U4B fill:#8BC34A,color:#fff
```

---

## 2. 핵심 기능별 상세 플로우

### 에코 맵 및 탄소 산출 플로우

```mermaid
flowchart LR
    S([시작]) --> MAP[지도 접속]
    MAP --> SEARCH[출발지/목적지 입력]
    SEARCH --> ROUTE[ORS 경로 계산 및 시각화]
    ROUTE --> CALC["탄소 절감량 계산\n(거리 * 0.21)"]
    CALC --> UI[결과 및 소나무 식재 효과 표시]
    UI --> AUTH{로그인 여부}
    AUTH -->|Yes| SAVE[활동 이력 저장 및 포인트 지급]
    AUTH -->|No| GUEST[조회 완료]

    style S fill:#4CAF50,color:#fff
    style CALC fill:#FF9800,color:#fff
    style SAVE fill:#2196F3,color:#fff
```

---

### 실시간 채팅 초대/참여 플로우 (Asymmetric Process)

```mermaid
flowchart TD
    HOST([방장]) --> INVITE[유저 초대]
    INVITE --> DB["CHAT_PARTICIPANTS (Status='PENDING')"]
    DB --> SOCKET["실시간 알림 전송 (WebSocket)"]
    
    USER([초대받은 유저]) --> NOTIF[알림 확인]
    NOTIF --> CHOICE{수락 여부}
    
    CHOICE -- "✅ 수락" --> ACCEPT["Status='ACCEPTED'\n공통 채널 구독 및 입장"]
    CHOICE -- "❌ 거절" --> REJECT["Status='REJECTED'\n초대 이력 삭제"]

    style HOST fill:#F44336,color:#fff
    style SOCKET fill:#FF9800,color:#fff
    style ACCEPT fill:#4CAF50,color:#fff
```

---

### 커뮤니티 거버넌스 (신고/블라인드 로직)

```mermaid
flowchart TD
    POST([게시글 노출]) --> VIEW[유저 열람]
    VIEW --> REPORT{유해 컨텐츠 신고}
    
    REPORT -->|신고 접수| COUNT["해당 게시글 신고 카운트 +1"]
    COUNT --> THRESHOLD{신고 10회 누적?}
    
    THRESHOLD -- "No" --> STAY[상태 유지]
    THRESHOLD -- "Yes" --> BLIND["자동 블라인드 처리\n(STATUS = 'BLIND')"]
    
    BLIND --> ADMIN[관리자 검토 목록 추가]

    style POST fill:#2196F3,color:#fff
    style BLIND fill:#F44336,color:#fff
    style ADMIN fill:#9E9E9E,color:#fff
```

---

## 3. 통합 서비스 라이프사이클

```mermaid
flowchart TD
    S([시작]) --> DAILY[데일리 체크: 출석 & 퀴즈]
    DAILY --> INFO[AI 기상 조언 및 뉴스 확인]
    INFO --> MAP[에코 맵 기반 상점 방문 / 이동]
    MAP --> REWARD[리워드 획득 및 에코트리 성장]
    REWARD --> SHARE[커뮤니티 활동 및 채팅 소통]
    SHARE --> S

    style DAILY fill:#E8F5E9
    style INFO fill:#E3F2FD
    style MAP fill:#FFF3E0
    style REWARD fill:#F1F8E9
    style SHARE fill:#F3E5F5
```
