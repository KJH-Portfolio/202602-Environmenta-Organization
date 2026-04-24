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

<details>
<summary><b>1. 기본 정보 (Info) 📅</b></summary>
<br>

- **📅 개발 기간:** 2026.03 ~ 2026.04 (약 2개월)
- **🖥️ 플랫폼:** Web (Responsive Support)
- **👥 인원 구성:** 개인 프로젝트
- **🛠️ Tech Stack:**
  - **Backend:** `Spring Boot 3.2`, `Java 17`, `MyBatis`, `Spring Security 6`
  - **Frontend:** `React 18`, `Context API`, `Axios Interceptor`
  - **Database:** `Oracle 19c`, `Caffeine Cache`
  - **Real-time:** `STOMP over WebSocket`, `SockJS`
  - **AI & Cloud:** `Google Gemini AI (Gemma-3)`, `WebClient (Reactive)`
  - **DevOps:** `Docker`, `Docker Compose`, `Swagger (OpenAPI 3.0)`

</details>

---

<details>
<summary><b>2. 프로젝트 전체 설계 (Architecture) 📊</b></summary>
<br>

**🎯 1. 프로젝트 목표**
> 탄소 발자국 추적과 AI 비서 기능을 결합하여, 사용자가 일상에서 환경 보호 활동을 즐겁게 실천하고 가시적인 리워드를 얻을 수 있는 선순환 환경 생태계를 구축합니다.

**📊 2. ERD (Entity Relationship Diagram & Data Dictionary)**
- 실시간 채팅 세션, 탄소 정밀 수치 기록, 신고 기반 자정 시스템을 통합 설계한 DB 청사진
- 👉 **[전체 데이터 설계도 (erd.md) 보러가기](./erd.md)**

**🗺️ 3. IA (Information Architecture) 및 기술 아키텍처**
- 서비스 계층 구조, React 컴포넌트 설계, 그리고 실시간/AI 인프라를 정의한 기술 설계도
- 👉 **[사이트 구조도 (ia.md)](./ia.md)** | **[리액트 구조 (react_structure.md)](./react_structure.md)** | **[인프라 설계 (infrastructure_architecture.md)](./infrastructure_architecture.md)**
- 👉 **[핵심 API 명세서 (api_specification.md) 보러가기](./api_specification.md)**

**🌊 4. User Flow**
- JWT 보안 필터링, ORS 기반 경로 계산, 실시간 소켓 핸드쉐이킹 등 핵심 로직의 흐름도
- 👉 **[유저 행동 흐름 (userflow.md) 보러가기](./userflow.md)**

**🔑 5. Environment Setup**
- 초기 데이터 현황 및 시연용 테스트 계정 정보 명세
- 👉 **[초기 데이터 및 테스트 계정 (init_data.md) 보러가기](./init_data.md)**

</details>

---

<details id="core-contributions">
<summary><b>3. 프로젝트 개인 구현 - 백엔드 설계 및 비즈니스 정합성 [CORE] 💎</b></summary>
<br>

> [!TIP]  
> **[백엔드 엔지니어링 문제 해결 보고서 (troubleshooting_deep_dive.md) 보러가기](./troubleshooting_deep_dive.md)**

### 🛡️ 1) JWT 기반 Stateless 인증 시스템 (Security)
- **Bearer 토큰 인증 (`JwtFilter`)**: `OncePerRequestFilter`를 상속받아 HTTP 요청의 `Authorization` 헤더에서 Bearer 토큰을 추출하고 유효성을 검증하는 전처리 필터 구현.
- **보안 무결성**: 성공 시 `UsernamePasswordAuthenticationToken`을 생성하여 `SecurityContextHolder`에 저장함으로써 세션 없이도 안정적인 사용자 인증 상태 유지.

### 🌏 2) 정밀 탄소 알고리즘 및 위치 기반 서비스 (Map & Algo)
- **Eco-Route 알고리즘**: `OpenRouteService(ORS)` API와 연동하여 실시간 이동 경로를 탐색하고 시각화하는 지리 정보 시스템 구축.
- **환경 수치 정밀화**: `거리 * 0.21(탄소 계수)` 공식을 적용하여 Oracle `NUMBER(10,3)` 타입으로 소수점 셋째 자리까지 오차 없는 탄소 절감량 계산 및 기록.

### 💰 3) Eco-Wallet 및 리워드 생태계 (Economy)
- **원자적 보상 처리**: 퀴즈, 출석 등 활동 보상 시 포인트 증감과 이력 기록을 `@Transactional`로 묶어 데이터 불일치 가능성을 원천 차단.
- **EcoTree 성장 모델**: 누적 경험치(XP)에 따른 실시간 레벨업 로직과 단계별 나무 성장 이미지를 동적으로 제공하는 게이미피케이션 요소 구현.

</details>

---

<details id="technical-deepdive">
<summary><b>4. 기술적 깊이 - 성능 최적화 및 AI 파이프라인 [DEEP DIVE] 🔥</b></summary>
<br>

### 🤖 1) Gemini AI 연동 및 비동기 데이터 파이프라인
- **비차단형 AI 통신**: `WebClient`를 활용한 Non-blocking 방식으로 Google Gemini AI와 통신하여 실시간 환경 가이드 멘트 생성.
- **커스텀 파일 캐시**: 빈번한 API 호출로 인한 오버헤드를 방지하기 위해 생성된 AI 응답을 로컬 JSON 파일로 캐싱하는 `FileCacheService`를 구축하여 응답 속도 및 비용 최적화.

### 💬 2) 고성능 실시간 메시징 엔진 (WebSocket)
- **STOMP 브로드캐스팅**: WebSocket 상위의 STOMP 프로토콜을 도입하여 방 단위의 구독(Subscription) 시스템 구축 및 메시지 라우팅 최적화.
- **실시간 상호작용**: 읽음 상태 추적 알고리즘을 통해 참여자별 안 읽은 메시지(Unread Count)를 실시간 산출하는 고속 쿼리 설계.
- **커서 기반 페이징**: 대용량 대화 내역 조회 시 성능 저하를 방지하기 위해 `MSG_ID` 기반의 Cursor-based Paging을 적용하여 무한 스크롤 구현.

### 🔌 3) 프론트엔드 상태 동기화 및 방어적 아키텍처
- **Cross-tab Sync**: 브라우저의 `Storage Event`를 감시하여 여러 탭 간 로그인/로그아웃 상태를 0.5초 이내에 완벽 동기화.
- **Axios Interceptor**: 401/403 에러 발생 시 중앙 집중형 토큰 정리 및 로그인 페이지 리다이렉션 로직을 구축하여 클라이언트 보안 강화.

</details>

---

<details id="retrospective-growth">
<summary><b>5. 회고 - 프로젝트 성찰 및 향후 지향점 [GROWTH] 🌟</b></summary>
<br>

- **🟢 Keep**: 백엔드 표준 MVC 레이어 분리 및 `@Transactional`을 통한 데이터 무결성 확보 습관.
- **🔴 Problem**: 실시간 채팅량이 증가할 때 단일 서버 웹소켓 세션 관리의 한계와 메모리 부하 가능성 식별.
- **🔵 Try**: 차기 프로젝트에서는 **Redis Pub/Sub**과 메시지 큐를 도입하여 웹소켓 서버의 수평 확장이 가능한 MSA 지향적 아키텍처로 고도화할 계획.

</details>

---

<details>
<summary>부록: 프로젝트 실행 방법 (Docker)</summary>

1. **사전 준비**: Docker Desktop 설치 및 실행
2. **빌드 및 실행**
   ```bash
   docker-compose up -d --build
   ```
3. **접속 주소**
   - Frontend: `http://localhost:3000`
   - Backend API (Swagger): `http://localhost:8080/swagger-ui/index.html`

</details>
