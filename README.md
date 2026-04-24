# 🌍 EasyEarth (이지어스)

> 🌱 **탄소 발자국 추적과 AI 환경 비서를 통해 지속 가능한 지구를 만드는 친환경 라이프스타일 플랫폼** 🌏

<br>

<p align="center">
  <img src="https://img.shields.io/badge/Java_17-ED8B00?style=flat-square&logo=openjdk&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring_Boot_3.2-6DB33F?style=flat-square&logo=springboot&logoColor=white" />
  <img src="https://img.shields.io/badge/React_18-61DAFB?style=flat-square&logo=react&logoColor=black" />
  <img src="https://img.shields.io/badge/Oracle_19c-F80000?style=flat-square&logo=oracle&logoColor=white" />
  <img src="https://img.shields.io/badge/Gemini_AI-4285F4?style=flat-square&logo=google&logoColor=white" />
  <img src="https://img.shields.io/badge/Docker-2496ED?style=flat-square&logo=docker&logoColor=white" />
</p>

<div align="center">
  <!-- 프로젝트 메인 이미지 또는 시연 GIF 배치 권장 -->
  <img src="https://via.placeholder.com/800x450.png?text=EasyEarth+Main+Dashboard" width="90%" alt="메인 화면 미리보기">
</div>

---

> [!IMPORTANT]  
> **Stateless 인증 체계부터 실시간 AI 분석 파이프라인까지, 풀스택 아키텍처의 성능과 보안을 동시에 잡은 고도화된 친환경 플랫폼입니다.**  
> - 💎, 🔥, 🌟 마크는 **핵심 기술적 역량과 문제 해결 능력을 증명하는 지표**입니다.  
> - 모든 내용이 접혀있으므로, 기여도가 높은 **3번(CORE)과 4번(DEEP DIVE) 섹션** 위주로 확인을 권장드립니다.
>   - 💎 **[CORE]**: JWT 기반 보안 아키텍처 및 정밀 탄소 계산 로직
>   - 🔥 **[DEEP DIVE]**: WebSocket 성능 최적화 및 Gemini AI 연동 파이프라인
>   - 🌟 **[GROWTH]**: 기술적 성찰과 차기 아키텍처 지향점

---

## 📅 1. 기본 정보 (Info)
- **개발 기간:** 2026.03 ~ 2026.04 (약 2개월)
- **플랫폼:** Web (Responsive)
- **인원 구성:** 개인 프로젝트 (또는 팀 구성 명시)
- **Tech Stack:**
  - **Backend:** `Spring Boot 3.2`, `Java 17`, `MyBatis`, `Spring Security 6`
  - **Frontend:** `React 18`, `Context API`, `Axios Interceptor`
  - **Database:** `Oracle 19c`, `Caffeine Cache`
  - **Real-time:** `STOMP over WebSocket`, `SockJS`
  - **AI & Cloud:** `Google Gemini AI (Gemma-3)`, `WebClient (Reactive)`
  - **DevOps:** `Docker`, `Docker Compose`, `Swagger (OpenAPI 3.0)`

---

## 📊 2. 프로젝트 전체 설계 (Architecture)
> 프로젝트의 논리적 구조와 데이터 흐름을 시각화한 상세 문서들입니다.

- **📊 ERD (Entity Relationship Diagram & Data Dictionary)**
  - 도메인 간 관계 설계 및 컬럼별 상세 제약조건, 시퀀스, 인덱스 전략을 통합한 데이터 설계 청사진
  - 👉 **[전체 데이터 설계도 (erd.md) 보러가기](./erd.md)**

- **🗺️ IA (Information Architecture)**
  - GNB 중심의 서비스 계층 및 페이지별 접근 권한(Role) 정의
  - 👉 **[사이트 구조도 (ia.md) 보러가기](./ia.md)**

- **🌊 User Flow**
  - 핵심 비즈니스 로직(경로 계산 ➡️ 탄소 산출 ➡️ 리워드)의 흐름 및 예외 처리
  - 👉 **[유저 행동 흐름 (userflow.md) 보러가기](./userflow.md)**

- **🔑 Environment Setup**
  - 초기 데이터 현황 및 테스트 계정 정보 명세
  - 👉 **[초기 데이터 및 테스트 계정 (init_data.md) 보러가기](./init_data.md)**

---

<details id="core-contributions">
<summary><b>3. 프로젝트 개인 구현 - 백엔드 설계 및 비즈니스 정합성 [CORE] 💎</b></summary>
<br>

### 🛡️ 1) JWT 기반 Stateless 인증 시스템 (Security)
- **Bearer 토큰 인증 (`JwtFilter`)**: `OncePerRequestFilter`를 통한 모든 HTTP 요청 가로채기 및 유효성 검증.
- **보안 무결성**: `SecurityContextHolder` 연동을 통해 세션 없이도 안정적인 유저 컨텍스트 유지 및 `BCrypt` 단방향 해시 암호화 적용.

### 🌏 2) 정밀 탄소 알고리즘 및 위치 기반 서비스 (Map & Algo)
- **Eco-Route 알고리즘**: `OpenRouteService(ORS)`와 `ODsay` 데이터를 융합하여 도보/자전거/대중교통 경로 시각화.
- **환경 수치 산출**: `거리 * 0.21(탄소 계수)` 및 `절감량 / 6.6(소나무 흡수량)` 공식을 적용한 실시간 환경 기여도 정밀 계산.

### 💰 3) Eco-Wallet 및 리워드 생태계 (Economy)
- **트랜잭션 기반 보상**: 퀴즈, 퀘스트, 출석 등 활동 발생 시 포인트 가감 및 이력 기록의 원자적 처리 보장.
- **EcoTree 성장 모델**: 누적 XP에 따른 레벨업 및 단계별 나무 이미지 동적 렌더링 로직 구현.

</details>

---

<details id="technical-deepdive">
<summary><b>4. 기술적 깊이 - 성능 최적화 및 AI 파이프라인 [DEEP DIVE] 🔥</b></summary>
<br>

### 🤖 1) Gemini AI 연동 및 비동기 데이터 파이프라인
- **AI 비서 멘트 생성**: `WebClient`를 활용한 Non-blocking 통신으로 Gemini AI 호출. 날씨/환경 데이터를 기반으로 한 프롬프트 엔지니어링 수행.
- **성능 최적화 (File Cache)**: 빈번한 AI 호출을 줄이기 위해 생성된 멘트를 로컬 파일 시스템에 JSON으로 직렬화하여 저장하는 커스텀 `FileCacheService` 구축.

### 💬 2) 고성능 실시간 메시징 엔진 (WebSocket)
- **STOMP 브로드캐스팅**: `/topic` 및 `/queue` 경로 설정을 통한 효율적인 메시지 라우팅 및 세션 관리 최적화.
- **Unread Count 알고리즘**: 참여자별 마지막 읽은 메시지 ID를 추적하여 실시간 안 읽은 메시지 개수를 계산하는 고속 쿼리 설계.
- **커서 기반 페이징 (Cursor-based)**: 대용량 대화 내역 조회 시 성능 저하 방지를 위해 커서 ID를 활용한 무한 스크롤 구현.

### 🔌 3) 프론트엔드 상태 동기화 및 에러 핸들링
- **Multi-layer Sync**: `Storage Event`와 `Interval Tracker`를 결합하여 다중 브라우저 탭 간 로그인 상태 완벽 동기화.
- **Axios Interceptor**: 401/403 에러 발생 시 중앙 집중형 토큰 정리 및 자동 로그인 유도 로직 구축.

</details>

---

<details id="retrospective-growth">
<summary><b>5. 회고 - 프로젝트 성찰 및 향후 지향점 [GROWTH] 🌟</b></summary>
<br>

- **🟢 Keep**: 백엔드의 표준 MVC 패턴 준수 및 `@Transactional`을 통한 데이터 무결성 확보 습관.
- **🔴 Problem**: 실시간 알림 기능이 많아지면서 발생하는 웹소켓 세션 관리의 복잡도 증가.
- **🔵 Try**: 차기 아키텍처에서는 **Redis Pub/Sub**을 도입하여 웹소켓 서버의 수평 확장을 고려한 메시지 브로커 구조로 고도화할 계획.

</details>

---

## 📦 6. 실행 방법 (How to Run)
1. **Prerequisites**: Docker Desktop, Java 17
2. **Setup**:
   ```bash
   git clone [레포지토리 주소]
   cd EasyEarth
   docker-compose up -d --build
   ```
3. **Access**: `http://localhost:3000` (React) / `http://localhost:8080/swagger-ui/index.html` (API Docs)
