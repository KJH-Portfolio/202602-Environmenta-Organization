---
작성일: 2026-04-27T18:50
수정일: 2026-04-30T00:30
---
# EasyEarth 사용자 비즈니스 동선 정의 (User Flow)

> **사용자 경험(UX) 중심의 기능 프로세스 및 예외 처리 아키텍처**  
> 이 문서는 실시간 채팅, AI 환경 비서, 퀘스트/퀴즈 참여, 에코 포인트 상점, 에코맵 탐색 등 플랫폼의 핵심 비즈니스 로직별 사용자 이동 경로와 시스템 자동화 프로세스를 다이어그램을 통해 정의합니다.

---

## 목차
1. [역할별 권한 플로우](#1-역할별-권한-플로우)
2. [핵심 기능별 상세 플로우](#2-핵심-기능별-상세-플로우)
3. [시스템 자동화 로직 (Automation)](#3-시스템-자동화-로직-automation)
4. [통합 비즈니스 플로우 (Full Flow)](#4-통합-비즈니스-플로우-full-flow)

---

## 1. 역할별 권한 플로우

### 비로그인 사용자 (Public)

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

### 일반 사용자 (MEMBER)

```mermaid
flowchart TD
    START([로그인 및 JWT 획득]) --> EVENT[전역 STOMP 채널 자동 구독]
    EVENT --> MAIN[메인 페이지]

    MAIN --> M1["💬 실시간 채팅"]
    MAIN --> M2["🌤️ 날씨 / 뉴스 / AI 비서"]
    MAIN --> M3["🗺️ 에코맵 탐색"]
    MAIN --> M4["📝 에코 커뮤니티"]
    MAIN --> M5["🎮 퀘스트 / 퀴즈 / 출석"]
    MAIN --> M6["🛒 포인트 상점 / 에코트리"]

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

    %% ── 에코맵 ──
    M3 --> T1[카테고리 선택]
    T1 --> T2[서울시 공공데이터 렌더링]
    T2 --> T3[마커 클릭 → 상세 정보 / 리뷰 작성]

    style START fill:#4CAF50,color:#fff
    style EVENT fill:#FF9800,color:#fff
```

---

## 2. 핵심 기능별 상세 플로우

### 2.1 실시간 채팅 및 알림 플로우 (WebSocket/STOMP)

로그인 시점에 전역 알림 채널을 구독하여, 사용자가 어떤 페이지에 있든 실시간 알림을 수신합니다.

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

### 2.2 AI 비서 및 글로벌 뉴스 파이프라인

외부 API 의존도와 응답 지연을 최소화하기 위해 FileCache 기반의 2-레이어 캐싱 전략을 채택했습니다.

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

### 2.3 데일리 퀘스트 플로우 (사진 인증)

매일 초기화되는 퀘스트 목록에서 사진을 업로드하여 인증하면 포인트가 적립됩니다. 중복 인증은 서버에서 차단됩니다.

```mermaid
flowchart LR
    S([마이페이지 진입]) --> LOAD["GET /api/quest/daily<br/>오늘의 퀘스트 목록 조회<br/>(완료 여부 포함)"]
    LOAD --> LIST["퀘스트 목록 렌더링<br/>(완료 항목은 비활성화)"]
    
    LIST --> SELECT["미완료 퀘스트 선택"]
    SELECT --> UPLOAD["사진 촬영 / 업로드"]
    UPLOAD --> API["POST /api/quest/certify/{questNo}<br/>사진 + userId 전송"]
    
    API --> DUPCHECK{"이미 인증했는가?"}
    DUPCHECK -->|"400: 중복 인증"| ERR["에러 메시지 반환<br/>'이미 완료한 퀘스트입니다'"]
    DUPCHECK -->|미인증| POINT["포인트 지갑 적립<br/>에코트리 경험치 반영"]
    POINT --> DONE["'인증 완료! 포인트 지급' 응답"]

    style S fill:#4CAF50,color:#fff
    style DUPCHECK fill:#FF9800,color:#fff
    style POINT fill:#2196F3,color:#fff
```

### 2.4 환경 퀴즈 플로우 (난이도별 일일 제한)

난이도별(Easy/Normal/Hard)로 하루 1회 참여 제한이 있습니다. 정답 시 포인트가 차등 지급됩니다.

```mermaid
flowchart LR
    S([퀴즈 페이지 진입]) --> STATUS["GET /api/quiz/status<br/>오늘 난이도별 참여 가능 여부 조회"]
    STATUS --> BTN{"버튼 활성화 상태"}
    BTN -->|이미 참여| DISABLED["해당 난이도 버튼 비활성화"]
    BTN -->|참여 가능| SELECT["난이도 선택<br/>(Easy 100P / Normal 200P / Hard 300P)"]
    
    SELECT --> QUIZ["GET /api/quiz/{difficulty}<br/>난이도별 퀴즈 문항 조회"]
    QUIZ --> ANSWER["답 선택 후 제출"]
    ANSWER --> SUBMIT["POST /api/quiz/attempt<br/>(userId, quizNo, isCorrect, point)"]
    
    SUBMIT --> RESULT{"정답 여부"}
    RESULT -->|정답| REWARD["포인트 지급<br/>에코트리 경험치 반영"]
    RESULT -->|오답| MSG["오답 안내 / 해설 출력"]
    REWARD --> NEXT["다음 퀴즈 또는 종료"]

    style S fill:#4CAF50,color:#fff
    style RESULT fill:#FF9800,color:#fff
    style REWARD fill:#2196F3,color:#fff
```

### 2.5 출석 체크 플로우 (연속 출석 보너스)

하루 1회 출석 체크로 포인트를 획득합니다. 중복 체크는 서버에서 `-1` 반환으로 차단됩니다.

```mermaid
flowchart LR
    S([마이페이지 / 출석 버튼 클릭]) --> API["POST /attendance/check<br/>(userId)"]
    
    API --> CHECK{"오늘 이미 출석했는가?"}
    CHECK -->|"-1 반환"| FAIL["'이미 출석 완료' 메시지 표시<br/>버튼 비활성화"]
    CHECK -->|미출석| CALC["연속 출석일 계산<br/>포인트 지급 (기본 + 연속 보너스)"]
    CALC --> WALLET["포인트 지갑 UPDATE<br/>POINT_TRANSACTIONS 이력 기록"]
    WALLET --> CAL["GET /attendance/list<br/>캘린더에 출석 이력 렌더링"]

    style S fill:#4CAF50,color:#fff
    style CHECK fill:#FF9800,color:#fff
    style WALLET fill:#2196F3,color:#fff
```

### 2.6 에코트리 성장 플로우 (포인트 연동)

회원 가입 시 트리거로 자동 생성된 에코트리가 누적 포인트에 따라 성장합니다.

```mermaid
flowchart LR
    S([회원 가입]) --> TRG["DB 트리거 자동 실행<br/>ECO_TREE 레코드 생성 (Lv.1)"]
    TRG --> IDLE["에코트리 대기 상태<br/>(초기 포인트 2,000P)"]
    
    IDLE --> ACT["퀘스트/퀴즈/출석으로 포인트 획득"]
    ACT --> GROW["POST /ecotree/grow/{memberId}<br/>누적 포인트 → 경험치 반영"]
    GROW --> LEVEL{"레벨업 조건 충족?"}
    LEVEL -->|"Yes"| UP["레벨 UP! 새 나무 스테이지로 변경"]
    LEVEL -->|"No"| SHOW["현재 성장 상태 시각화<br/>GET /ecotree/{memberId}"]
    UP --> SHOW

    style S fill:#4CAF50,color:#fff
    style TRG fill:#8BC34A,color:#fff
    style LEVEL fill:#FF9800,color:#fff
```

### 2.7 포인트 상점 & 랜덤 뽑기 플로우

포인트로 뱃지/배경/칭호를 구매하거나, 랜덤 뽑기로 아이템을 획득합니다. 중복 뽑기 시 500P 환급됩니다.

```mermaid
flowchart LR
    S([상점 진입]) --> ITEM["GET /items/select<br/>전체 아이템 목록 조회"]
    ITEM --> FILTER["카테고리 / 등급 필터링<br/>(BADGE / BACKGROUND / TITLE)"]
    
    FILTER --> MODE{"구매 방식 선택"}
    MODE -->|직접 구매| BUY["POST /items/buy<br/>(itemId, userId, price)"]
    MODE -->|랜덤 뽑기| PULL["GET /items/random/{memberId}<br/>1,000P 소진"]
    
    BUY --> PCHECK{"포인트 충분?"}
    PCHECK -->|"400: 포인트 부족"| ERR["'포인트가 부족합니다' 반환"]
    PCHECK -->|충분| SUCCESS["아이템 소유 등록<br/>포인트 차감"]
    
    PULL --> RARITY{"확률 테이블<br/>COMMON 25% / RARE 25%<br/>EPIC 25% / LEGENDARY 25%"}
    RARITY --> DUPCHECK{"중복 아이템?"}
    DUPCHECK -->|"중복"| REFUND["500P 환급<br/>'중복 아이템 당첨, 500P 환급' 안내"]
    DUPCHECK -->|"신규"| SUCCESS

    SUCCESS --> EQUIP["PATCH /items/{itemId}/equip<br/>아이템 장착 / 해제 토글"]

    style S fill:#4CAF50,color:#fff
    style MODE fill:#FF9800,color:#fff
    style RARITY fill:#9C27B0,color:#fff
```

### 2.8 에코맵 탐색 및 리뷰 플로우 (공공데이터 동적 동기화)

서울시 공공데이터를 실시간으로 불러오며, 사용자가 처음 클릭한 장소는 DB에 자동 저장됩니다.

```mermaid
flowchart LR
    S([에코맵 진입]) --> CAT["카테고리 선택<br/>(제로웨이스트샵 / 재활용 정거장 / 약수터 등)"]
    CAT --> FETCH["서울시 공공데이터 API 호출<br/>(contsId 기반)"]
    FETCH --> RENDER["카카오맵 마커 클러스터 렌더링"]
    
    RENDER --> CLICK["마커 클릭"]
    CLICK --> SYNC{"로컬 DB에 해당 장소 존재?"}
    SYNC -->|"최초 클릭"| INSERT["ECO_SHOP 레코드 자동 생성<br/>(contsId 기준 자동 동기화)"]
    SYNC -->|"기존 장소"| DETAIL["장소 상세 정보 표시"]
    INSERT --> DETAIL
    
    DETAIL --> REVIEW["리뷰 조회<br/>GET /eco/review/list/{shopId}"]
    REVIEW --> WRITE{"리뷰 작성 시도"}
    WRITE -->|"비로그인"| LOGIN["로그인 유도"]
    WRITE -->|"로그인"| POST["POST /eco/review/write<br/>평점 + 내용 저장"]
    POST --> AVG["DB 트리거 자동 실행<br/>AVG_RATING 즉시 재계산"]

    style S fill:#4CAF50,color:#fff
    style SYNC fill:#FF9800,color:#fff
    style AVG fill:#2196F3,color:#fff
```

---

## 3. 시스템 자동화 로직 (Automation)

프로젝트의 운영 효율성을 높이고 외부 통신 지연을 방지하기 위해 백엔드에서 자동으로 수행되는 로직입니다.

### 3.1 파일 캐시 자동 갱신 (DataScheduler)
외부 API의 쿼터 제한을 피하고 즉각적인 응답 속도를 제공하기 위해 서버 스케줄러가 백그라운드에서 데이터를 수집 및 가공합니다.

```mermaid
flowchart LR
    SCHEDULER([Cron: 매 정각 30분 / 6시간마다]) --> TASK1[날씨 데이터 갱신]
    SCHEDULER --> TASK2[글로벌 뉴스 갱신]
    
    TASK1 --> GEMINI[Gemini AI 가공 / 번역]
    TASK2 --> GEMINI
    
    GEMINI --> UPDATE[로컬 캐시 파일 Overwrite]
    
    style SCHEDULER fill:#FF9800,color:#fff
```

### 3.2 이력 자동 정리 (HistoryScheduler)
매일 자정 `INTEGRATED_HISTORY` 테이블에 남은 미완료(`P: Pending`) 이력을 일괄 삭제하여 다음날 새로운 퀘스트/퀴즈 참여 기회를 보장합니다.

```mermaid
flowchart LR
    CRON([Cron: 매일 자정 00:00]) --> QUERY["INTEGRATED_HISTORY에서<br/>당일 'P' 상태 레코드 조회"]
    QUERY --> DELETE["만료된 Pending 이력 일괄 삭제"]
    DELETE --> RESET["다음날 퀘스트 / 퀴즈 할당 준비 완료"]

    style CRON fill:#FF9800,color:#fff
```

---

## 4. 통합 비즈니스 플로우 (Full Flow)

```mermaid
flowchart TD
    S([사이트 접속]) --> MAIN[메인 페이지]

    MAIN --> AUTH{"로그인 상태 검증<br/>(JWT)"}

    AUTH -->|"비로그인 (Public)"| GUEST["조회 전용<br/>(날씨/뉴스/커뮤니티 목록)"]
    AUTH -->|"인가됨 (MEMBER)"| USER[전체 기능 이용 가능]

    %% 비로그인 → 로그인 유도
    GUEST -->|채팅/퀘스트 등 접근| LOGIN[로그인 / 카카오 연동]
    LOGIN -->|성공| USER

    %% 일반 사용자 기능
    USER --> U1["💬 실시간 채팅망 접속"]
    USER --> U2["🌤️ AI 환경 비서 소통"]
    USER --> U3["🎮 퀘스트 / 퀴즈 / 출석 참여"]
    USER --> U4["🗺️ 에코맵 탐색 및 리뷰"]
    USER --> U5["🛒 포인트 상점 / 에코트리"]

    U1 --> R1["양방향 소통 및 안 읽음 알림 수신"]
    U2 --> R2["맞춤형 환경 뉴스 및 AI 조언 획득"]
    U3 --> R3["포인트 획득 → 지갑 적립"]
    U4 --> R4["제로웨이스트 장소 탐색 및 리뷰 기여"]
    U5 --> R5["아이템 장착 → 에코트리 레벨업"]

    style S fill:#4CAF50,color:#fff
    style MAIN fill:#2196F3,color:#fff
    style AUTH fill:#FF9800,color:#fff
    style GUEST fill:#9E9E9E,color:#fff
```
