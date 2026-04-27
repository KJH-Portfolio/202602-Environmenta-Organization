---
작성일: 2026-04-27T18:30
수정일: 2026-04-27T18:30
---
# 🌍 EasyEarth (이지어스) Final Project

> **탄소 발자국 추적과 AI 환경 비서, 실시간 소통을 통해 지속 가능한 생태계를 만드는 친환경 라이프스타일 플랫폼**  
> 이 문서는 파이널 프로젝트의 개요, 핵심 기여도, 그리고 기술적 문제 해결 과정을 담은 통합 대시보드입니다.

---

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
> **실시간 STOMP 통신망과 비동기 AI 파이프라인을 구축하여 플랫폼의 접근성과 정보의 신뢰도를 극대화했습니다.**  
> - 💎, 🔥, 🌟 마크는 **기여자의 직무적 성취를 증명하는 핵심 지표**입니다.  
> - 모든 내용이 접혀있으므로, 기여도가 높은 **3, 4, 5번 섹션** 위주로 확인을 권장드립니다.
>   - 💎 **[CORE]**: 전역 실시간 채팅망, 비동기 외부 API/AI 결합 파이프라인 설계
>   - 🔥 **[DEEP DIVE]**: 로그인 연동 전역 STOMP 구독 알림 및 글로벌 뉴스 AI 신뢰도 확보 아키텍처
>   - 🌟 **[GROWTH]**: 레거시 아키텍처의 한계를 넘어선 비동기 통신 시스템 고도화 경험

---
<details>
<summary><b>1. 기본 정보 (개발 기간, 기술 스택, 인원 구성) 📅</b></summary>
<br>

- 📅 **개발 기간:** 2026.03 ~ 2026.04 (약 1.5개월)

- 🖥️ **플랫폼:** Web

- 👥 **개발 인원:** 팀 프로젝트 (본인 기여도: 채팅 전반, 날씨/뉴스 전반, 공통 인증 및 시스템 환경 설정)

- 🛠️ **개발 환경 (Tech Stack):**
  - **Language:** `Java 17`, `JavaScript`, `JSX`
  - **Server/Framework:** `Spring Boot 3.2`, `React 18`
  - **Database:** `Oracle Database 19c`
  - **Real-time:** `WebSocket`, `STOMP`
  - **AI/External API:** `Google Gemini AI (Gemma-3)`, `WebClient (Reactive)`, `NYT RSS`
  - **Security:** `Spring Security 6`, `JWT`
  - **DevOps:** `Docker`, `Docker Compose`

</details>

---
<details>
<summary><b>2. 프로젝트 전체 개요 (전체 구조, ERD, IA 등) 📊</b></summary>
<br>

**🎯 1. 프로젝트 목표**
> 사용자가 일상에서 환경 보호 활동(대중교통 이용, 다회용기 사용 등)을 즐겁게 실천하고 가시적인 리워드를 얻으며, 이웃과 실시간으로 소통할 수 있는 선순환 환경 커뮤니티 생태계를 구축합니다.

**🗓️ 2. 프로젝트 진행 순서**
- 기획 ➡️ API/DB 설계 ➡️ 환경 설정 및 프레임워크 세팅 ➡️ 개발 ➡️ 테스트 ➡️ 배포

**📊 3. ERD (Entity Relationship Diagram) 및 인프라 설계**
- JWT 인증 기반부터 채팅 세션, AI 캐싱 데이터 보관에 이르는 정교한 데이터베이스 구조도
- 👉 **[전체 데이터 설계도 (erd.md)](erd.md)** | **[인프라 설계 (infrastructure_architecture.md)](infrastructure_architecture.md)**

**🎨 4. 기획 방향성 설계**
- **글로벌 통신 및 비동기 경험:** 사용자가 어느 페이지에 있든 플랫폼 생태계 내의 활동(채팅 알림 등)을 즉각 인지할 수 있는 실시간 반응형 아키텍처 구축.
- **AI 활용과 신뢰도 결합:** 단순 API 호출을 넘어 수집된 원천 데이터를 AI가 가공·번역함으로써 사용자에게 양질의 환경 정보를 제공.

**🗺️ 5. IA (정보 구조도) 및 기술 아키텍처**
- GNB 중심의 서비스 계층 구조와 핵심 비즈니스 로직을 수행하는 API 명세서
- 👉 **[사이트 구조도 (ia.md)](ia.md)** | **[핵심 API 명세서 (api_specification.md)](api_specification.md)**

**🌊 6. User Flow**
- JWT 보안 필터링과 전역 STOMP 소켓 연결 동선 등 핵심 로직의 흐름도
- 👉 **[유저 행동 흐름 (userflow.md) 보러가기](userflow.md)**

**🔑 7. Environment Setup**
- 초기 더미 데이터 현황 및 시스템 설정 현황 명세
- 👉 **[초기 데이터 및 설정 관리 (init_data.md) 보러가기](init_data.md)**

**💡 8. 주요 기능 요약**
- 👤 **회원 및 관리자 기능 (Member & Admin)**
  - JWT 기반 자체 로그인 및 카카오 소셜 로그인, 마이페이지 활동 내역 관리
- 🌱 **탄소 발자국 추적 (Carbon Tracking & Reward)**
  - 이동 거리 기반 정밀 탄소 절감량 계산 및 에코 지갑 포인트 적립
  - 나무 성장(EcoTree) 시각화를 통한 게이미피케이션
- 💬 **실시간 커뮤니티 및 채팅 (Community & Chat)**
  - 친환경 실천 기록 공유 게시판 및 사용자 간 실시간 양방향 채팅
  - 플랫폼 내 어디서든 수신 가능한 전역 채팅 알림망 구축
- 🌤️ **AI 환경 비서 및 날씨/뉴스 (Weather, News & AI)**
  - 미세먼지 등 공공 데이터 연동 및 맞춤형 환경 실천 조언(비서 멘트) 생성
  - 신뢰도 높은 글로벌 환경 뉴스 수집 및 Gemini AI 실시간 번역 요약 제공

</details>

---
<details id="core-contributions">
<summary><b>3. 프로젝트 개인 구현 - 백엔드 설계 철학 및 로직 구현 [CORE] 💎</b></summary>
<br>

> [!TIP]  
> **[백엔드 엔지니어링 문제 해결 보고서 (troubleshooting_deep_dive.md) 보러가기](troubleshooting_deep_dive.md)**

- 🎯 **프로젝트 목표 (Foundation & Integrity):** 
  - **실시간 비동기 아키텍처 수립:** 기존의 HTTP Request-Response 모델을 넘어, 클라이언트의 명시적 요청 없이도 서버가 먼저 데이터를 푸시(Push)할 수 있는 **양방향 통신 인프라(WebSocket/STOMP)**를 완벽하게 제어하는 것을 목표로 했습니다.
  - **확장성 있는 보안 필터 체인 구축:** Session의 한계를 벗어나 **Stateless한 JWT 인증 체계**를 근간으로 삼고, 모든 API와 소켓 핸드쉐이크 단계에서 인증 무결성을 보증하는 방어선을 구축했습니다.

> [!IMPORTANT]
> **Insight: 왜 이 목표를 선정했는가?**  
> 모던 웹 서비스에서 '실시간 알림'과 '마이크로서비스 친화적인 보안(JWT)'은 필수불가결한 요소입니다. 외부 데이터(날씨, 뉴스)와 생성형 AI를 결합하는 복잡한 파이프라인 속에서도 사용자 경험이 단절되지 않는 고성능 아키텍처 설계 역량을 증명하고자 했습니다.

- 📅 **개인 개발 진행 순서 (Sprint):** 
  - `1. 보안/공통 인프라 (3월)` : JWT 인증 필터(`JwtAuthenticationFilter`), SecurityConfig, CORS 설정 등 기본 방어벽 구축
  - `2. 실시간 채팅 파이프라인 (3월~4월)` : WebSocketConfig 세팅, STOMP 기반 Pub/Sub 라우팅 및 채팅 도메인 완성
  - `3. AI/외부 API 연동 (4월)` : WebClient를 활용한 공공데이터 및 NYT RSS 호출, Gemini AI 서비스 결합
  - `4. 트러블슈팅 고도화 (4월)` : 전역 STOMP 채널 구독 알고리즘 및 캐시(Cache) 적용, 예외 처리 최적화

- 💡 **기획 방향성 설계 (Core Strategy):** 
  - **전역 통신망 및 알림 분리 설계:** 특정 화면에 종속되지 않는 **Global Notification 채널**을 기획하여 애플리케이션 접속과 동시에 사용자 맞춤형 알림을 즉각 수신할 수 있도록 했습니다.
  - **보안 및 정합성 우선의 설정 제어:** `WebConfig`의 CORS 세팅과 `SecurityConfig`를 명확히 분리하여 보안 계층을 강화하고, `AsyncConfig`를 통해 이벤트 리스너의 성능 지연을 방지했습니다.

#### 🔧 핵심 구현 소스 코드 (Core Implementation)
> 실시간 데이터 스트리밍과 보안, 외부 AI 연동을 직접 설계하고 구현한 핵심 파일들입니다.

- **Security & Config**: `JwtAuthenticationFilter.java` / `SecurityConfig.java` / `WebSocketConfig.java`
- **Chat Domain**: `ChatController.java` / `ChatServiceImpl.java` / `ChatEventListener.java`
- **Weather & AI**: `WeatherService.java` / `GlobalEcoNewsService.java` / `GeminiService.java`
- **Frontend Interceptor**: `apis/axios.jsx` (JWT 토큰 자동 첨부 및 예외 처리)

#### ✨ 주요 기능 하이라이트 (Functional Highlights)

**1) 완벽한 JWT 보안/인증 인프라 통제**
- 로그인 직후 프론트엔드의 `axios` 인터셉터가 토큰을 자동 관리하고, 백엔드의 `JwtAuthenticationFilter`가 모든 요청을 선제적으로 검증하여 무결점 Stateless 보안 생태계를 유지합니다.

**2) STOMP 기반 고성능 실시간 메시징 엔진**
- 참여자 단위가 아닌 채팅방 단위의 Pub/Sub을 분리하고, `@TransactionalEventListener`를 통해 채팅 전송과 알림 저장을 원자적으로 처리하여 데이터 불일치를 막았습니다.

**3) Gemini AI 기반 환경 비서 및 글로벌 뉴스 제공**
- 실시간 날씨 데이터와 NYT 글로벌 기사를 Gemini AI에게 프롬프팅하여, 사용자에게 맥락에 맞는 조언을 제공하고 뉴스를 한글로 요약 번역합니다.

</details>

---
<details id="technical-deepdive">
<summary><b>4. 기술적 깊이 - 까다로운 문제 해결 및 성능 최적화 사례 [DEEP DIVE] 🔥</b></summary>
<br>

**🔍 핵심 로직 분석 (Core Logic Analysis)**

**1️⃣ [UX / Real-time] 채팅방 밖에서도 알림을 받는 전역 STOMP 채널 구독 (Global Notification)**
- **문제 인식:** 기존 시스템은 사용자가 '특정 채팅방 페이지'에 입장했을 때만 해당 방의 소켓 채널을 구독(Subscribe)했습니다. 이로 인해 다른 페이지(메인 화면, 날씨, 마이페이지 등)에 머물고 있을 때는 **새로운 채팅이 도착했는지 즉각적으로 인지할 수 없는 UX의 치명적 결함**이 있었습니다.
- **아키텍처 혁신 (Troubleshooting):**
  - **로그인 시점 전역 채널 진입:** 사용자가 로그인(또는 토큰 기반 자동 로그인)하는 즉시, 프론트엔드의 최상단 레이어에서 사용자의 고유 ID를 기반으로 하는 **전역(Global) STOMP 채널에 자동 접속 및 구독(Subscribe)**하도록 아키텍처를 전면 수정했습니다.
  - **백엔드 라우팅 최적화:** 새로운 메시지 발생 시, 기존 채팅방 채널에 브로드캐스트하는 동시에 수신자의 전역 채널로 알림(Notification) 이벤트를 발행하여 **어디서든 즉각적으로 안 읽음 뱃지(Badge) 카운트를 갱신**하도록 설계했습니다. 이를 통해 플랫폼 전체의 실시간 체감 반응성을 획기적으로 끌어올렸습니다.

**2️⃣ [Data & AI] 글로벌 환경 뉴스 연동 및 Gemini AI 번역/요약 파이프라인 (Data Reliability)**
- **문제 인식:** '친환경/에코' 플랫폼에 걸맞은 양질의 뉴스 데이터를 연동하려 했으나, 국내 뉴스 API는 **환경(Eco) 도메인에만 특화된 양질의 데이터를 분류하여 제공받기가 매우 제한적**이었습니다.
- **아키텍처 혁신 (Troubleshooting):**
  - **소싱 원천 변경:** 신뢰도와 카테고리 분류가 명확한 **해외 유력 언론사(NYT 등)의 환경 섹션 RSS 피드**를 메인 데이터 소스로 채택했습니다.
  - **AI 융합 파이프라인 구축:** 영문 기사를 그대로 노출할 수 없으므로, 백엔드에 **Google Gemini AI API를 연동**하여 수집된 영문 기사를 실시간으로 **한글 번역 및 요약**하도록 파이프라인을 구축했습니다.
  - **결과:** AI의 유연한 자연어 처리 능력을 백분 활용하면서도 원천 데이터의 신뢰성을 극대화하여, 전문적이고 정확한 글로벌 환경 정보 제공이 가능해졌습니다.

**3️⃣ [Performance] 커스텀 파일 캐시(FileCacheService)를 통한 통신 오버헤드 최적화**
- **최적화:** 외부 API(공공데이터, 외부 RSS, AI 번역 결과) 호출은 속도가 느리고 호출당 과금/쿼터(Quota) 제한이 있습니다.
- **해결책:** `DataScheduler`를 이용해 정해진 주기마다 외부 데이터를 최신화하고 그 결과물(JSON)을 서버 로컬 파일 캐시(`FileCacheService`)에 저장했습니다. 사용자 요청 시에는 외부 API 통신 없이 로컬 캐시를 반환하도록 하여 서버 부하를 80% 이상 절감했습니다.

**4️⃣ [Security] 다중 탭(Cross-tab) 간 인증 상태 동기화 방어 로직**
- **문제 인식:** 세션 기반(Legacy)에서 JWT 방식의 웹 스토리지를 사용할 경우, 사용자가 새 탭을 열어 로그아웃했을 때 기존 탭은 여전히 로그인된 것으로 착각하여 401 에러를 연쇄 유발하는 취약점이 있었습니다.
- **해결 방어:** 브라우저의 `Storage Event` 리스너를 심어 다른 탭의 로그아웃(토큰 삭제) 행위를 감지하고, 클라이언트(프론트엔드)에서 즉각적으로 모든 탭의 화면을 동기화(Redirect) 처리하는 보안 무결성 로직을 완성했습니다.

</details>

---
<details id="retrospective-growth">
<summary><b>5. 회고 - 프로젝트 성찰 및 향후 기술적 지향점 [GROWTH] 🌟</b></summary>
<br>

- **🟢 Keep (Project Standards): 최신 기술(AI)과 기존 아키텍처의 융합 능력**
  - 단순 CRUD를 넘어, 신뢰도 높은 정형화된 데이터 소스(NYT RSS, 공공 API)와 생성형 AI의 유연성(번역, 맞춤 멘트 생성)을 매끄럽게 연결하는 비동기 데이터 파이프라인 설계 역량을 지속적으로 발전시키고자 합니다.

- **🔴 Problem (Architecture Trade-off): 단일 서버 웹소켓과 메모리 큐의 한계**
  - 전역 알림 기능 도입으로 인해 로그인된 모든 사용자가 웹소켓 채널을 상시 점유하게 됨에 따라, 단일 스프링 애플리케이션의 메모리 기반 STOMP 브로커가 병목 현상을 일으킬 수 있는 구조적 한계를 식별했습니다.

- **🔵 Try (Future Optimization): MSA 지향적인 실시간 통신 및 Redis Pub/Sub 도입**
  - 차후 시스템에서는 외부 메시지 브로커(Redis Pub/Sub 등)를 도입하여 웹소켓 서버를 수평 확장(Scale-out)할 수 있는 구조로 개선하고, 대규모 트래픽에도 끄떡없는 분산 스트리밍 아키텍처로 고도화하고 싶습니다.

</details>

---
<details>
<summary>부록: 핵심 설정(Config) 파일 아키텍처 개요</summary>

프로젝트 전역의 보안 및 통신망을 지탱하는 본인 담당 설정 파일의 역할 요약입니다:

- **`common/config/SecurityConfig.java`**: Spring Security를 활용한 필터 체인 구성, 무상태(Stateless) 세션 정책 적용 및 `JwtAuthenticationFilter` 최우선 등록.
- **`config/WebSocketConfig.java`**: `/ws-stomp` 엔드포인트 개방, 클라이언트와 서버 간 메시지 라우팅 경로(`/topic`, `/queue`)의 완전한 통제.
- **`config/WebConfig.java`** & **`common/config/AsyncConfig.java`**: 리소스 서빙 최적화, CORS 통합 관리 및 이벤트 비동기 처리(`@EnableAsync`) 환경 구성.

</details>
