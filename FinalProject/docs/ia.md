# EasyEarth 프로젝트 IA (Information Architecture)

> **사이트 전체 페이지 계층 구조 및 기능 정의**  
> Mermaid `graph TD` 문법을 활용하여 서비스의 전체 흐름과 접근 권한을 정의합니다.

---

## 목차
1. [전체 사이트 구조 (Overview)](#1-전체-사이트-구조-overview)
2. [도메인별 상세 구조](#2-도메인별-상세-구조)
   - [🌏 에코 맵 (Map & Route)](#에코-맵-map--route)
   - [💬 실시간 채팅 (Messaging)](#실시간-채팅-messaging)
   - [📝 커뮤니티 (Community)](#커뮤니티-community)
   - [🌱 마이페이지 (My & Growth)](#마이페이지-my--growth)

---

## 1. 전체 사이트 구조 (Overview)

상단 내비게이션 바(GNB)를 중심으로 한 메인 계층 구조입니다.

```mermaid
graph TD
    ROOT["🏠 EasyEarth 메인"]
    ROOT --> NAV_MAP["🌏 에코 맵"]
    ROOT --> NAV_CHAT["💬 실시간 채팅"]
    ROOT --> NAV_COMM["📝 커뮤니티"]
    ROOT --> NAV_MY["🌱 마이페이지 / 에코트리"]
    ROOT --> NAV_AUTH["👤 로그인 / 회원가입"]

    style ROOT fill:#4CAF50,color:#fff,stroke:#388E3C
    style NAV_MAP fill:#2196F3,color:#fff
    style NAV_CHAT fill:#FF9800,color:#fff
    style NAV_COMM fill:#9C27B0,color:#fff
    style NAV_MY fill:#8BC34A,color:#fff
    style NAV_AUTH fill:#607D8B,color:#fff
```

---

## 2. 도메인별 상세 구조

### 에코 맵 (Map & Route)
위치 기반 상점 조회 및 탄소 절감 경로 계산을 담당합니다.

```mermaid
graph LR
    SUB_M["🌏 에코 맵 메인"] --> M1["상점 목록 / 검색"]
    M1 --> M2["상점 상세 보기"]
    M2 --> M3["리뷰 조회 / 작성"]
    
    SUB_M --> M4["경로 탐색 (ORS)"]
    M4 --> M5["이동수단별 탄소 수치 계산"]
    M5 --> M6["에코 경로 결과 시각화"]

    style SUB_M fill:#2196F3,color:#fff
```

### 실시간 채팅 (Messaging)
유저 간 실시간 소통 및 커뮤니케이션을 담당합니다.

```mermaid
graph LR
    SUB_C["💬 채팅 메인"] --> C1["참여 중인 채팅방 목록"]
    C1 --> C2{방 입장}
    C2 -->|"🔒 로그인"| C3["실시간 메시징 (WebSocket)"]
    C3 --> C4["메시지 검색 / 상단 공지"]
    C3 --> C5["멤버 관리 / 초대 / 강퇴"]
    
    C1 --> C6{방 개설}
    C6 -->|"🔒 로그인"| C7["채팅방 설정 (이미지/제목)"]

    style SUB_C fill:#FF9800,color:#fff
```

### 커뮤니티 (Community)
환경 보호 활동 공유 및 자유로운 정보 교환을 담당합니다.

```mermaid
graph LR
    SUB_B["📝 커뮤니티 메인"] --> B1["게시판 목록\n(공지/자유/정보/신고)"]
    B1 --> B2["게시글 상세 조회"]
    B2 --> B3["댓글 / 대댓글 작성"]
    B2 --> B4["좋아요 / 신고하기"]
    
    B1 --> B5{글쓰기}
    B5 -->|"🔒 로그인"| B6["에디터 (파일 첨부)"]

    style SUB_B fill:#9C27B0,color:#fff
```

### 마이페이지 (My & Growth)
개인 활동 이력 관리 및 에코트리 성장을 담당합니다.

```mermaid
graph TD
    subgraph MY ["🌱 마이페이지 (Private)"]
        MY1["내 프로필 관리"]
        MY2["에코트리 대시보드\n(XP/Level 시각화)"]
        MY3["활동 내역 조회\n(포인트/참여 퀘스트)"]
        MY4["데일리 체크\n(출석/퀴즈/인증)"]
    end

    style MY fill:#F1F8E9,stroke:#8BC34A,color:#333
```

---

## 페이지 목록 및 매핑 명세

| 영역 | 페이지 역할 | Mapping URL | 로그인 | 접근 권한 |
|---|---|---|---|---|
| **메인** | 대시보드/AI 가이드 | `/dashboard` | ❌ | 공통 |
| **에코 맵** | 지도/상점 조회 | `/map`, `/map/detail` | ❌ | 공통 |
| **에코 맵** | 경로 계산 | `/map/route` | ❌ | 공통 |
| **채팅** | 룸 목록/메시징 | `/chat`, `/chat/room/:id` | ✅ | 일반회원 |
| **커뮤니티** | 게시판/상세 | `/community`, `/community/:id` | ❌ | 공통 |
| **커뮤니티** | 글쓰기 | `/community/write` | ✅ | 일반회원 |
| **마이페이지** | 에코트리/프로필 | `/mypage`, `/profile` | ✅ | 일반회원 |
| **마이페이지** | 퀴즈/퀘스트 | `/activity/quiz`, `/activity/quest` | ✅ | 일반회원 |
