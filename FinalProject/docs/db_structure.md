# EasyEarth 프로젝트 DB 구조 분석 (Data Dictionary)

> **Technical Note: 데이터 설계 및 정합성 유지 원칙**
> - **수치 정밀도 (Precision)**: 탄소 절감량(`distance * 0.21`)과 같은 환경 수치는 데이터 손실 방지를 위해 Oracle `NUMBER(10, 3)` 타입을 적용하여 소수점 셋째 자리까지 관리합니다.
> - **보안 기반 설계**: 비밀번호는 `BCrypt` 10 rounds 암호화를 필수로 하며, `Stateless` 인증을 지원하기 위한 사용자 식별 구조를 갖춥니다.
> - **이력 보존 전략**: 포인트 지급 및 게시글 상태 변경 시 정합성을 위해 별도의 히스토리 테이블 또는 `STATUS` 컬럼을 활용한 논리적 관리를 수행합니다.

---

## ✅ 주요 도메인 데이터 현황 (Mockup/Initial)

| 테이블 그룹 | 핵심 데이터 | 설계 목적 |
|---|---|---|
| `MEMBERS` | 👤 계정 및 온라인 상태 | 보안 및 실시간 소셜 인터렉션의 기초 |
| `CHAT_MESSAGES` | 💬 메시지 및 읽음 상태 | **커서 기반 페이징** 및 Unread Count 산출 |
| `ECO_SHOPS` | 🗺️ 공공 데이터 상점 | 오프라인 환경 활동의 거점 데이터 |
| `BOARDS` | 📝 커뮤니티 및 신고 | 자정 작용(Blind System)을 통한 클린 커뮤니티 |
| `POINT_HISTORIES` | 💰 경제 생태계 이력 | 사용자의 모든 활동 보상에 대한 신뢰성 확보 |

---

## 🔑 주요 컬럼 제약사항 및 명세

| 테이블 | 컬럼 | 타입 | 제약조건 | 설명 / 비고 |
|---|---|---|---|---|
| `MEMBERS` | `USER_PWD` | VARCHAR2(100) | NN | **BCrypt** 단방향 해시 암호화 적용 |
| `MEMBERS` | `ONLINE_STATUS` | CHAR(1) | DEFAULT 'N' | 'Y'(온라인), 'N'(오프라인) 실시간 동기화 |
| `ECO_TREES` | `CURRENT_XP` | NUMBER | DEFAULT 0 | 획득 경험치 (레벨업 로직과 연동) |
| `CHAT_MESSAGES`| `IS_DELETED` | CHAR(1) | DEFAULT 'N' | 'Y'(삭제됨), 'N'(정상) - Soft Delete |
| `CHAT_PARTICIPANTS`| `STATUS` | VARCHAR2(10) | NN | `PENDING`, `ACCEPTED`, `REJECTED` |
| `BOARDS` | `STATUS` | VARCHAR2(10) | NN | `NORMAL`, `BLIND` (신고 10회 누적 시) |
| `POINT_HISTORIES`| `AMOUNT` | NUMBER | NN | 증감 포인트 (+/-) |

---

## 🔄 테이블 관계 및 종속성 (Dependency)

```text
MEMBERS (USER_ID)
  ├── WALLETS (USER_ID)
  │     └── POINT_HISTORIES (WALLET_ID)
  ├── ECO_TREES (USER_ID)
  ├── CHAT_PARTICIPANTS (USER_ID)
  │     └── CHAT_ROOMS (ROOM_ID)
  │           └── CHAT_MESSAGES (ROOM_ID)
  ├── BOARDS (USER_ID)
  │     ├── COMMENTS (BOARD_ID)
  │     └── BOARD_REPORTS (BOARD_ID)
  └── SHOP_REVIEWS (USER_ID)
```

---

## 🏷️ 시퀀스(Sequence) 목록

시스템 전반의 고유 식별자 생성을 위한 시퀀스 설계입니다.

| 시퀀스명 | 적용 테이블.컬럼 | 설명 |
|---|---|---|
| `SEQ_MEMBER_NO` | `MEMBERS.MEMBER_NO` | 회원 일련번호 |
| `SEQ_CHAT_ROOM` | `CHAT_ROOMS.ROOM_ID` | 채팅방 ID |
| `SEQ_CHAT_MSG` | `CHAT_MESSAGES.MSG_ID` | 메시지 ID |
| `SEQ_BOARD_ID` | `BOARDS.BOARD_ID` | 게시글 ID |
| `SEQ_POINT_HIS` | `POINT_HISTORIES.HISTORY_ID` | 포인트 이력 ID |
| `SEQ_SHOP_ID` | `ECO_SHOPS.SHOP_ID` | 에코 상점 ID |

---

## 🔑 테스트 및 개발용 데이터 정보

| 분류 | 아이디 (ID) | 비밀번호 (PWD) | 역할 (Role) | 비고 |
|---|---|---|---|---|
| **관리자** | `admin` | `admin123` | ADMIN | 시스템 관리 및 신고 처리 |
| **일반유저** | `user01` | `pass01` | MEMBER | 탄소 포인트 1000pt 보유 |
| **테스트유저**| `tester` | `test1234` | MEMBER | 신규 가입 및 에코트리 1단계 |

> **Security Note**: 개발 환경에서는 보안을 위해 BCrypt 암호화를 기본으로 하며, 테스트 시 `PasswordConfig`를 참조하십시오.
