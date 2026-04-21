# EasyEarth 파이널 프로젝트 DB 구조 분석

> 분석일: 2026-04-21 (전수 조사 완료)
> 프로젝트: EasyEarth - 친환경 실천 게임화(Gamification) 플랫폼
> DB 계정: EASYEARTH / Oracle 21c, XEPDB1
> 구성: Spring Boot(REST API) + Vue.js(SPA) + Oracle 21c (Docker)

---

## 📊 전체 테이블 목록 (mapper XML 기준 전수 조사)

| # | 테이블명 | 설명 | 구현 여부 | 더미 필요 여부 |
|---|---|---|---|---|
| 1 | `MEMBER` | 회원 | ✅ `member-mapper.xml` | ✅ 필수 |
| 2 | `POINT_WALLET` | 포인트 지갑 | ✅ 트리거+quest/item/attendance-mapper에서 직접 UPDATE | ⚡ 트리거 자동 생성 |
| 3 | `ECO_TREE` | 에코 트리 성장 단계 | ✅ `ecotree-mapper.xml` | ⚡ 트리거 자동 생성 |
| 4 | `INTEGRATED_HISTORY` | 퀴즈/퀘스트 일일 할당·완료 통합 이력 | ✅ `history-mapper.xml` (핵심 테이블) | ✅ 필수 |
| 5 | `QUEST` | 친환경 퀘스트 목록 | ✅ `quest-mapper.xml` | ✅ 이미 삽입됨 (50개) |
| 6 | `QUEST_HISTORY` | 퀘스트 인증 내역 | ✅ `quest-mapper.xml` | ✅ 필요 |
| 7 | `QUIZ` | 환경 퀴즈 문항 | ✅ `quiz-mapper.xml` | ✅ 이미 삽입됨 (60문항) |
| 8 | `QUIZ_HISTORY` | 퀴즈 풀이 이력 (문항 단위) | ✅ `quiz-mapper.xml` | ✅ 필요 |
| 9 | `ATTENDANCE` | 출석 기록 | ✅ `attendance-mapper.xml` | ✅ 필요 |
| 10 | `ITEM` | 아이템 (뱃지/배경/칭호) | ✅ `item-mapper.xml` | ✅ 이미 삽입됨 (130개) |
| 11 | `USER_ITEMS` | 사용자 보유 아이템 (랜덤뽑기 포함) | ✅ `item-mapper.xml` | ✅ 필요 |
| 12 | `ECO_SHOP_CATEGORY` | 친환경 상점 카테고리 | ✅ `ecoshop-mapper.xml` | ✅ 이미 삽입됨 (7개) |
| 13 | `ECO_SHOP` | 친환경 상점 (서울시 API 연동) | ✅ `ecoshop-mapper.xml` | ✅ 필요 |
| 14 | `ECO_SHOP_REVIEW` | 상점 후기 | ✅ `ecoshop-mapper.xml` | ✅ 필요 |
| 15 | `COMMUNITY_POST` | 커뮤니티 게시글 | ✅ `community-mapper.xml` | ✅ 필수 |
| 16 | `COMMUNITY_REPLY` | 커뮤니티 댓글 | ✅ `community-mapper.xml` | ✅ 필수 |
| 17 | `COMMUNITY_POST_LIKES` | 게시글 좋아요 | ✅ `community-mapper.xml` | 선택 |
| 18 | `COMMUNITY_REPLY_LIKES` | 댓글 좋아요 | ✅ `community-mapper.xml` | 선택 |
| 19 | `POST_FILES` | 게시글 첨부파일 | ✅ `community-mapper.xml` | 선택 |
| 20 | `REPORTS` | 신고 | ✅ `reports-mapper.xml` | 선택 |
| 21 | `INQUIRIES` | 1:1 문의 / FAQ | ✅ `inquires-mapper.xml` | ✅ 필요 |
| 22 | `CHAT_ROOM` | 채팅방 | ✅ `chat` 패키지 | ✅ 필요 |
| 23 | `CHAT_ROOM_USER` | 채팅 참여자 | ✅ `chat` 패키지 | ✅ 필요 |
| 24 | `CHAT_MESSAGE` | 채팅 메시지 | ✅ `chat` 패키지 | ✅ 필요 |
| 25 | `MESSAGE_REACTION` | 메시지 리액션 | ✅ `chat` 패키지 (MessageReactionEntity) | 선택 |
| 26 | `MEMBER_IMPACT_SUMMARY` | 회원 환경 기여도 요약 | 🔶 별도 테이블 미사용 (`INTEGRATED_HISTORY` 실시간 집계로 대체) | ❌ 제외 |
| 27 | `IMPACT_GLOBAL_DAILY` | 전체 통계 일별 기록 | 🔶 별도 테이블 미사용 (`INTEGRATED_HISTORY` 실시간 집계로 대체) | ❌ 제외 |
| 28 | `ROUTE_COMPARE` | 길찾기 비교 저장 | 🔶 API는 구현됨(`/api/route/ors`, `/api/route/transit`), **DB 저장 안 함** | ❌ 제외 |
| 29 | `ECO_SHOP_SUGGESTION` | 상점 수정 제안 | ❌ 미구현 (mapper/코드 없음) | ❌ 제외 |
| 30 | `DAILY_QUEST` | 일일 퀘스트 (구형 스키마) | ❌ 미구현 (`INTEGRATED_HISTORY`가 완전 대체) | ❌ 제외 |
| 31 | `POINT_TRANSACTIONS` | 포인트 거래 내역 | ❌ 미구현 (POINT_WALLET 직접 수정으로 대체) | ❌ 제외 |
| 32 | `ECO_DIARY` | 환경 일기 | ❌ 미구현 (diary 패키지/mapper 없음) | ❌ 제외 |
| 33 | `ECO_DIARY_AI_REPLY` | 환경 일기 AI 답변 | ❌ 미구현 (diary 패키지/mapper 없음) | ❌ 제외 |

---

## ⚡ 트리거 자동 생성 테이블 (INSERT 직접 금지)

```
MEMBER INSERT 시 자동 처리:
  ├── POINT_WALLET  (TRG_MEMBER_REGISTERED)  → 초기 포인트 2,000 지급
  └── ECO_TREE      (TRG_MEMBER_ECO_TREE_INIT) → TREE_LEVEL=1, SYNCED_EXP=0
```

> ⚠️ MEMBER를 INSERT하면 POINT_WALLET과 ECO_TREE가 자동으로 생성됩니다.
> 별도로 INSERT하면 **중복/오류** 발생합니다.

---

## 🔑 핵심 구조: INTEGRATED_HISTORY

**init_db.sql 1327줄에서 직접 생성**되는 핵심 테이블.  
퀴즈 + 퀘스트의 **일일 할당 및 완료 여부**를 통합 관리하며,  
`MEMBER_IMPACT_SUMMARY` / `IMPACT_GLOBAL_DAILY` 역할을 **실시간 집계**로 대체함.

```sql
INTEGRATED_HISTORY
  ├── HISTORY_ID    NUMBER PK
  ├── MEMBER_ID     NUMBER (FK → MEMBER)
  ├── ACTIVITY_TYPE VARCHAR2(20)  -- 'QUEST', 'QUIZ_E', 'QUIZ_N', 'QUIZ_H'
  ├── REF_ID        NUMBER        -- QUEST_NO 또는 QUIZ_NO
  ├── STATUS        CHAR(1)       -- 'P'=진행중, 'Y'=완료, 'N'=실패
  ├── IMAGE_URL     VARCHAR2(500) -- 퀘스트 인증샷
  └── CREATED_AT    DATE
```

> **더미 데이터 필요**: `INTEGRATED_HISTORY`에 퀴즈/퀘스트 완료 이력을 넣어야
> `staticCache`의 CO2 절감량, 나무 효과 통계가 화면에 표시됨.

---

## ✅ 이미 삽입된 초기 데이터

| 테이블 | 데이터 | 비고 |
|---|---|---|
| `ECO_SHOP_CATEGORY` | 7개 카테고리 | 자전거 도로, 제로웨이스트 상점 등 |
| `ITEM` (BADGE) | COMMON 10 + RARE 10 + EPIC 10 + LEGENDARY 20 = **50개** | |
| `ITEM` (BACKGROUND) | COMMON 10 + RARE 10 + EPIC 10 + LEGENDARY 10 = **40개** | |
| `ITEM` (TITLE) | COMMON 10 + RARE 10 + EPIC 10 + LEGENDARY 10 = **40개** | |
| `QUIZ` | Easy 20 + Normal 20 + Hard 20 = **60문항** | CO2/나무 효과 값 포함 |
| `QUEST` | **50개** 퀘스트 | CO2/나무 효과 값, 포인트 100으로 일괄 설정됨 |
| `MEMBER` | **admin 1명** (MEMBER_ID: 1) | BCrypt 12 rounds |

---

## 🎯 더미 데이터 삽입 계획 (우선순위순)

### 1순위 - 필수

| 테이블 | 목표 건수 | 내용 |
|---|---|---|
| `MEMBER` | +6명 (총 7명) | user01~user06, BCrypt 암호화 |
| `INTEGRATED_HISTORY` | 30건 | 퀴즈/퀘스트 완료 이력 (통계 표시용) |
| `COMMUNITY_POST` | 20건 | 카테고리: 나눔/자유/인증/정보/기타 |
| `COMMUNITY_REPLY` | 20건 | 게시글별 댓글 |
| `QUEST_HISTORY` | 20건 | 퀘스트 인증 내역 |
| `QUIZ_HISTORY` | 20건 | 퀴즈 풀이 이력 (문항 단위) |
| `ATTENDANCE` | 15건 | 출석 기록 |

### 2순위 - 권장

| 테이블 | 목표 건수 | 내용 |
|---|---|---|
| `USER_ITEMS` | 15건 | 회원별 보유 아이템 (뽑기/구매) |
| `ECO_SHOP` | 10건 | 서울 친환경 상점 |
| `ECO_SHOP_REVIEW` | 15건 | 상점 후기 |
| `INQUIRIES` | 10건 | FAQ 5건 + 일반 문의 5건 |
| `CHAT_ROOM` | 3건 | 그룹 채팅방 |
| `CHAT_ROOM_USER` | 10건 | 채팅 참여자 |
| `CHAT_MESSAGE` | 20건 | 채팅 메시지 |

---

## 📝 주요 컬럼 제약사항

| 테이블 | 컬럼 | 타입 | 허용값 / 비고 |
|---|---|---|---|
| `MEMBER` | `STATUS` | VARCHAR2(20) | `'Y'` 사용 |
| `MEMBER` | `GENDER` | VARCHAR2(10) | `'M'` / `'F'` |
| `COMMUNITY_POST` | `STATUS` | VARCHAR2(1) | `'Y'` / `'N'` / `'B'` |
| `COMMUNITY_POST` | `CATEGORY` | VARCHAR2(30) | ⚠️ `'나눔'` / `'자유'` / `'인증'` / `'정보'` / `'기타'` (CHECK 제약 있음) |
| `COMMUNITY_REPLY` | `STATUS` | VARCHAR2(1) | `'Y'` / `'N'` / `'B'` |
| `COMMUNITY_REPLY` | `DEPTH` | NUMBER | `0`=원댓글, `1`=대댓글 |
| `QUEST_HISTORY` | `STATUS` | VARCHAR2(1) | `'Y'`=완료 (직접 'Y'로 삽입) |
| `QUIZ_HISTORY` | `CORRECT_YN` | CHAR(1) | `'Y'` / `'N'` |
| `INTEGRATED_HISTORY` | `STATUS` | CHAR(1) | `'P'`=진행중, `'Y'`=완료, `'N'`=실패 |
| `INTEGRATED_HISTORY` | `ACTIVITY_TYPE` | VARCHAR2(20) | `'QUEST'` / `'QUIZ_E'` / `'QUIZ_N'` / `'QUIZ_H'` |
| `CHAT_ROOM` | `ROOM_TYPE` | VARCHAR2(20) | `'SINGLE'` / `'GROUP'` |
| `CHAT_ROOM_USER` | `INVITATION_STATUS` | VARCHAR2(20) | `'PENDING'` / `'ACCEPTED'` / `'REJECTED'` (기본값 `'ACCEPTED'`) |
| `CHAT_MESSAGE` | `MESSAGE_TYPE` | VARCHAR2(20) | `'TEXT'` / `'IMAGE'` / `'EMOJI'` / `'ENTER'` / `'LEAVE'` / `'FILE'` |
| `ITEM` | `RARITY` | VARCHAR2(30) | `'COMMON'` / `'RARE'` / `'EPIC'` / `'LEGENDARY'` |
| `INQUIRIES` | `STATUS` | VARCHAR2(50) | `'SUBMITTED'` / `'PROCESSING'` / `'COMPLETED'` |
| `INQUIRIES` | `IS_PUBLIC` | CHAR(1) | `'Y'` / `'N'` |
| `INQUIRIES` | `IS_FAQ` | CHAR(1) | `'Y'` / `'N'` |
| `POINT_WALLET` | `NOW_POINT` | NUMBER | 초기값 2000 (트리거) |
| `ECO_TREE` | `TREE_LEVEL` | NUMBER | `1`~`4` (1=새싹, 4=열매) |

---

## 🌐 구현된 외부 API 연동

| 기능 | 패키지 | API | DB 저장 |
|---|---|---|---|
| 친환경 상점 지도 | `ecoshop` | 서울시 공공데이터 API | `ECO_SHOP` 저장 |
| 자동차/도보/자전거 경로 | `map` | ORS(OpenRouteService) API | ❌ 저장 안 함 |
| 대중교통 경로 | `map` | ODsay API | ❌ 저장 안 함 |
| 날씨 조회 | `weather` | 기상청 API | ❌ 저장 안 함 |
| AI 분리수거 안내 | `gemini` | Google Gemini API | ❌ 저장 안 함 |
| AI 날씨+환경 조언 | `gemini` | Google Gemini API | ❌ 저장 안 함 |

---

## 🔑 테스트 계정 정보

| 항목 | 값 |
|---|---|
| MEMBER_ID | 1 |
| LOGIN_ID | `admin` |
| PASSWORD | BCrypt 12 rounds 암호화 |
| 원본 비밀번호 | `admin` |
