---
작성일: 2026-04-27T18:40
수정일: 2026-04-27T18:40
---
# 🗺️ EasyEarth 파이널 프로젝트 IA (Information Architecture)

> **사용자 경험(UX) 중심의 페이지 계층 구조 및 API 서비스 명세**  
> 이 문서는 서비스의 주요 메뉴 구조와 내비게이션 흐름, 그리고 백엔드 API 엔드포인트를 기반으로 플랫폼의 정보 구조와 데이터 흐름을 정의합니다.

---

## 📑 목차
1. [📊 서비스 레이아웃 (Overview)](#1-서비스-레이아웃-overview)
2. [🐾 도메인별 상세 아키텍처](#2-도메인별-상세-아키텍처)
3. [📑 페이지 및 API 상세 명세](#3-페이지-및-api-상세-명세)

---

## 📊 1. 서비스 레이아웃 (Overview)

사이트의 핵심 메뉴는 GNB(Global Navigation Bar)를 통해 제어되며, 각 도메인은 React Router의 `PrivateRoute`, `PublicRoute` 등 권한 기반 가드 시스템을 거쳐 렌더링됩니다.

```mermaid
graph TD
    ROOT["🏠 EasyEarth Main<br/>(Home.jsx)"]
    ROOT --> NAV_CHAT["💬 실시간 채팅<br/>(/chat)"]
    ROOT --> NAV_NEWS["📰 환경 뉴스<br/>(GlobalEcoNews.jsx)"]
    ROOT --> NAV_COMM["📝 에코 커뮤니티<br/>(/community)"]
    ROOT --> NAV_MY["📋 마이페이지<br/>(/mypage)"]
    ROOT --> NAV_ADMIN["🔒 관리자 대시보드<br/>(AdminRoute)"]

    style ROOT fill:#4CAF50,color:#fff,stroke:#388E3C
    style NAV_CHAT fill:#FF9800,color:#fff
    style NAV_NEWS fill:#03A9F4,color:#fff
```

---

## 2. 도메인별 상세 아키텍처

### 💬 2.1 채팅 도메인 (Chat & Notification)
전역(Global) 알림을 수신하는 메인 소켓 채널과 각 채팅방 내부의 로컬 통신 채널이 병렬적으로 동작합니다.

```mermaid
graph LR
    C_LOGIN["🔐 로그인 완료<br/>(전역 STOMP 채널 자동 구독)"] --> C_MAIN["💬 채팅 메인<br/>(ChatRoomList.jsx)"]
    C_MAIN --> C_DETAIL["상세 보기<br/>(ChatRoomDetail.jsx)"]
    C_DETAIL --> C_PUB["메시지 발송<br/>(/app/chat/message)"]
    C_PUB --> C_EVENT["비동기 이벤트<br/>(@TransactionalEventListener)"]
    
    style C_LOGIN fill:#FF9800,color:#fff
    style C_EVENT fill:#F44336,color:#fff
```

### 🌤️ 2.2 날씨 및 뉴스 도메인 (Weather & AI News)
외부 자원(공공데이터, RSS 피드)을 서버 로컬 파일 캐시(FileCache)로 적재하고, Gemini AI를 통해 데이터를 가공하여 프론트엔드로 전달합니다.

```mermaid
graph LR
    W_MAIN["🌤️ 날씨 위젯<br/>(Home.jsx)"] --> W_API["기상청 데이터/캐시<br/>(WeatherService)"]
    W_API --> W_AI["비서 멘트 생성<br/>(GeminiService)"]
    N_MAIN["📰 뉴스 섹션<br/>(GlobalEcoNews.jsx)"] --> N_RSS["NYT RSS 수집<br/>(GlobalEcoNewsService)"]
    N_RSS --> N_TRANS["AI 요약 번역<br/>(GeminiService)"]

    style W_API fill:#03A9F4,color:#fff
    style N_RSS fill:#03A9F4,color:#fff
```

### 📝 2.3 에코 커뮤니티 (Community & Governance)
사용자 간의 환경 지식 공유 및 신고 시스템을 통한 거버넌스를 구축했습니다.

```mermaid
graph LR
    CO_LIST["📝 커뮤니티 리스트<br/>(/community)"] --> CO_DETAIL["게시글 상세<br/>(PostDetail.jsx)"]
    CO_DETAIL --> CO_REPLY["댓글 및 리액션<br/>(ReplySection.jsx)"]
    CO_DETAIL --> CO_REP["신고 처리<br/>(ReportsModal.jsx)"]

    style CO_LIST fill:#9C27B0,color:#fff
```

### 💰 2.4 경제 및 아이템 (Economy & Shop)
활동으로 획득한 포인트를 소비하고, 지도를 통해 친환경 매장을 탐색합니다.

```mermaid
graph LR
    S_MAP["🗺️ 에코 맵<br/>(EcoMap.jsx)"] --> S_DETAIL["매장 상세/리뷰<br/>(ShopDetail.jsx)"]
    S_MAP --> S_ROUTE["탄소 저감 경로 비교<br/>(RouteCompare.jsx)"]
    P_SHOP["🛍️ 아이템 샵<br/>(ItemShop.jsx)"] --> P_PULL["랜덤 가챠<br/>(GachaSystem.jsx)"]

    style S_MAP fill:#4CAF50,color:#fff
```

### 🌿 2.5 마이페이지 및 통계 (MyPage & Stats)
에코 다이어리와 AI 분석을 통해 사용자의 환경 기여도를 시각화합니다.

```mermaid
graph LR
    M_DIARY["📖 에코 다이어리<br/>(EcoDiary.jsx)"] --> M_AI["AI 감정/실천 분석<br/>(GeminiReply)"]
    M_STATS["📊 임팩트 통계<br/>(ImpactStats.jsx)"] --> M_TREE["에코 트리 성장<br/>(EcoTree.jsx)"]

    style M_STATS fill:#607D8B,color:#fff
```

---

## 📑 3. 페이지 및 API 상세 명세

| 도메인 | 기능(Feature) | URL 경로 (Endpoint) | 통신 방식 | 권한(Route Guard) |
|---|---|---|---|---|
| **채팅** | 채팅방 목록 및 생성 | `/chat` | **REST (JSON)** | PrivateRoute |
| **채팅** | 실시간 채팅 및 알림 채널 | `ws://domain/ws-stomp` | **STOMP (WebSocket)** | PrivateRoute |
| **날씨** | 전국 기상청 공공데이터 | `/weather/forecast` | REST (JSON) | PublicRoute |
| **AI 비서** | AI 기반 실천 조언 | `/gemini/secretary` | REST (JSON) | PublicRoute |
| **뉴스** | 글로벌 환경 뉴스 연동 | `/global/eco-news` | REST (JSON) | PublicRoute |
| **커뮤니티**| 게시글 필터 검색 | `/community/list` | REST (JSON) | PublicRoute |
| **커뮤니티**| 게시글 상세 및 댓글 | `/community/post/:id` | REST (JSON) | PublicRoute |
| **에코맵** | 주변 친환경 매장 검색 | `/shop/list` | REST (JSON) | PublicRoute |
| **에코맵** | 경로별 탄소 절감 비교 | `/route/compare` | REST (JSON) | PublicRoute |
| **아이템** | 가챠 및 아이템 구매 | `/item/purchase` | REST (JSON) | PrivateRoute |
| **다이어리**| AI 일기 분석 요청 | `/diary/analyze` | REST (JSON) | PrivateRoute |
| **통계** | 개인 환경 임팩트 리포트 | `/member/impact` | REST (JSON) | PrivateRoute |
| **인증** | 카카오 소셜 연동 | `/api/auth/kakao` | REST (JSON) | PublicRoute |
| **보안** | JWT 토큰 갱신 | `/api/auth/refresh` | REST (JSON) | PrivateRoute |
| **회원** | 프로필 및 지갑 내역 | `/member/profile` | REST (JSON) | PrivateRoute |

---

### 💡 문서 활용 가이드
- **STOMP (WebSocket)**: 클라이언트의 지속적인 폴링(Polling) 없이 서버가 클라이언트로 데이터를 즉시 푸시(Push)할 수 있는 실시간 양방향 통신 계층입니다.
- **REST (JSON)**: 프론트엔드의 전역 인터셉터(`apis/axios.jsx`)를 통해 `Authorization: Bearer` 헤더가 자동 첨부되어 통신합니다.
- **Route Guard**: React Router의 `PrivateRouter` 및 `AuthContext`를 통해 토큰 만료 또는 비인가 사용자의 페이지 접근을 원천 차단합니다.
