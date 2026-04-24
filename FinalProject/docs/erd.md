# EasyEarth 프로젝트 ERD (Entity Relationship Diagram)

> **Mermaid `erDiagram` 기반 설계**  
> 이 다이어그램은 5대 핵심 도메인(회원, 채팅, 지도, 커뮤니티, 게이미피케이션) 간의 유기적인 데이터 관계를 시각화합니다.

---

## 📊 1. 전체 도메인 관계도 (Overview)

```mermaid
erDiagram

    %% ──────────────────────────
    %%  1. 회원 및 경제 생태계 (Member & Wallet)
    %% ──────────────────────────
    MEMBERS {
        varchar USER_ID PK
        varchar USER_PWD
        varchar USER_NAME
        varchar USER_NICKNAME
        char    ONLINE_STATUS "Y/N"
        varchar USER_ROLE "ADMIN/MEMBER"
        date    ENROLL_DATE
    }

    WALLETS {
        number  WALLET_ID PK
        varchar USER_ID FK
        number  CURRENT_POINTS
        number  TOTAL_ACCUMULATED
    }

    POINT_HISTORIES {
        number  HISTORY_ID PK
        number  WALLET_ID FK
        varchar ACTION_TYPE "QUIZ/QUEST/ATTENDANCE"
        number  AMOUNT
        date    CREATED_AT
    }

    %% ──────────────────────────
    %%  2. 실시간 메시징 (Real-time Chat)
    %% ──────────────────────────
    CHAT_ROOMS {
        number  ROOM_ID PK
        varchar TITLE
        varchar THUMBNAIL_URL
        varchar CREATOR_ID FK
        date    CREATED_AT
    }

    CHAT_MESSAGES {
        number  MSG_ID PK
        number  ROOM_ID FK
        varchar SENDER_ID FK
        varchar CONTENT
        char    IS_DELETED "Y/N"
        date    CREATED_AT
    }

    CHAT_PARTICIPANTS {
        number  PARTICIPANT_ID PK
        number  ROOM_ID FK
        varchar USER_ID FK
        number  LAST_READ_MSG_ID "Unread Count 계산용"
        varchar ROLE "HOST/MEMBER"
        varchar STATUS "PENDING/ACCEPTED/REJECTED"
    }

    %% ──────────────────────────
    %%  3. 에코 맵 및 경로 (Map & Route)
    %% ──────────────────────────
    ECO_SHOPS {
        number  SHOP_ID PK
        varchar CONTS_ID "공공데이터 연동키"
        varchar SHOP_NAME
        varchar CATEGORY "제로웨이스트/수거함 등"
        varchar ADDRESS
        number  LAT
        number  LON
    }

    SHOP_REVIEWS {
        number  REVIEW_ID PK
        number  SHOP_ID FK
        varchar USER_ID FK
        varchar CONTENT
        number  RATING
        date    CREATED_AT
    }

    %% ──────────────────────────
    %%  4. 커뮤니티 및 거버넌스 (Community)
    %% ──────────────────────────
    BOARDS {
        number  BOARD_ID PK
        varchar USER_ID FK
        varchar CATEGORY
        varchar TITLE
        varchar CONTENT
        number  VIEW_COUNT
        varchar STATUS "NORMAL/BLIND"
        date    CREATED_AT
    }

    COMMENTS {
        number  COMMENT_ID PK
        number  BOARD_ID FK
        varchar USER_ID FK
        number  PARENT_ID FK "대댓글 (Self-join)"
        varchar CONTENT
        date    CREATED_AT
    }

    BOARD_REPORTS {
        number  REPORT_ID PK
        number  BOARD_ID FK
        varchar REPORTER_ID FK
        varchar REASON
        date    CREATED_AT
    }

    %% ──────────────────────────
    %%  5. 게이미피케이션 (Gamification)
    %% ──────────────────────────
    ECO_TREES {
        number  TREE_ID PK
        varchar USER_ID FK
        number  CURRENT_LEVEL
        number  CURRENT_XP
        varchar TREE_IMAGE_URL
    }

    ATTENDANCE {
        number  ATT_ID PK
        varchar USER_ID FK
        date    ATT_DATE
        number  CONTINUOUS_DAYS
    }

    %% ══════════════════════════
    %%  관계 정의 (Relationships)
    %% ══════════════════════════

    %% 회원 기반
    MEMBERS ||--|| WALLETS : "소유"
    WALLETS ||--o{ POINT_HISTORIES : "기록"
    MEMBERS ||--|| ECO_TREES : "성장"
    MEMBERS ||--o{ ATTENDANCE : "체크"

    %% 채팅 도메인
    MEMBERS ||--o{ CHAT_ROOMS : "개설"
    CHAT_ROOMS ||--o{ CHAT_PARTICIPANTS : "참여"
    MEMBERS ||--o{ CHAT_PARTICIPANTS : "가입"
    CHAT_ROOMS ||--o{ CHAT_MESSAGES : "송신"
    MEMBERS ||--o{ CHAT_MESSAGES : "작성"

    %% 지도 및 리뷰
    ECO_SHOPS ||--o{ SHOP_REVIEWS : "후기"
    MEMBERS ||--o{ SHOP_REVIEWS : "작성"

    %% 커뮤니티
    MEMBERS ||--o{ BOARDS : "작성"
    BOARDS ||--o{ COMMENTS : "댓글"
    MEMBERS ||--o{ COMMENTS : "작성"
    COMMENTS ||--o{ COMMENTS : "대댓글(Self)"
    BOARDS ||--o{ BOARD_REPORTS : "신고"
    MEMBERS ||--o{ BOARD_REPORTS : "접수"
```

---

## 🔄 도메인 계층 구조 (Hierarchy View)

> `MEMBERS` 테이블을 중심으로 한 서비스별 데이터 종속성 구조입니다.

```text
MEMBERS (USER_ID)
  ├── WALLETS (USER_ID)
  │     └── POINT_HISTORIES (WALLET_ID)
  ├── ECO_TREES (USER_ID)
  ├── ATTENDANCE (USER_ID)
  ├── CHAT_PARTICIPANTS (USER_ID)
  │     └── CHAT_ROOMS (ROOM_ID)
  │           └── CHAT_MESSAGES (ROOM_ID)
  ├── BOARDS (USER_ID)
  │     ├── COMMENTS (BOARD_ID)
  │     └── BOARD_REPORTS (BOARD_ID)
  └── SHOP_REVIEWS (USER_ID)
```

---

## 📋 테이블 그룹 요약

| 그룹 | 테이블 | 비고 |
|---|---|---|
| 👤 **Identity** | `MEMBERS`, `WALLETS` | 보안 및 경제 생태계의 기초 |
| 💬 **Real-time** | `CHAT_ROOMS`, `CHAT_MESSAGES`, `CHAT_PARTICIPANTS` | WebSocket 기반 실시간 동기화 |
| 🗺️ **Location** | `ECO_SHOPS`, `SHOP_REVIEWS` | 공공 데이터 싱크 및 유저 피드백 |
| 📝 **Governance** | `BOARDS`, `COMMENTS`, `BOARD_REPORTS` | 자정 작용(신고) 및 계층형 소통 |
| 🌱 **Growth** | `ECO_TREES`, `ATTENDANCE` | 사용자 리텐션 및 게이미피케이션 |

---

## ⚡ DB 성능 최적화 전략 (Index Strategy)

조회 성능 극대화 및 커서 기반 페이징을 위해 다음과 같은 인덱스를 설계했습니다.

| 대상 테이블 | 대상 컬럼 | 인덱스 종류 | 기대 효과 |
|---|---|---|---|
| `CHAT_MESSAGES` | `ROOM_ID`, `MSG_ID` | 복합 인덱스 | **커서 기반 페이징** 시 정렬 및 필터링 속도 비약적 향상 |
| `CHAT_PARTICIPANTS` | `ROOM_ID`, `USER_ID` | Unique Index | 중복 참여 방지 및 룸별 멤버 고속 조회 |
| `COMMENTS` | `BOARD_ID`, `PARENT_ID` | Non-Unique | 계층형 대댓글 트리 구조 렌더링 성능 최적화 |
| `BOARD_REPORTS` | `BOARD_ID` | Non-Unique | **Blind System** 처리를 위한 신고 횟수 카운트 성능 개선 |
| `MEMBERS` | `ONLINE_STATUS` | Bitmap Index | 실시간 접속 유저 필터링 및 소셜 상태 동기화 최적화 |
