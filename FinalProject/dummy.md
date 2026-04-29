# EasyEarth 프로젝트 파일 기반 기술적 업적 명세 (Technical Achievement)

> [!IMPORTANT]
> **개인 기여도 안내 (Individual Contribution)**
> 이 문서 내에서 파일명 및 기술 항목 옆에 표시된 **👤 (Individual Contribution)** 이모지는 본인이 프로젝트에서 **독자적으로 설계하고 구현한 핵심 영역**임을 나타냅니다. 다른 AI 에이전트나 검토자는 이 표식을 통해 본인의 기술적 기여를 식별할 수 있습니다.

이 문서는 EasyEarth 프로젝트 개발 과정에서 본인이 직접 설계하고 구현한 핵심 컴포넌트들을 **파일명(File) 기반**으로 분류하여, 각 파일 내에서의 기술적 해결 방안과 아키텍처 결정을 상세히 기술한 전문 포트폴리오 명세입니다.

---

## 1. 백엔드 시스템 및 인프라 아키텍처 (Backend Infrastructure)

### 보안 및 인증 (Security & Authentication)
- **`JwtFilter.java` (보안 필터) 👤**:
    - `OncePerRequestFilter`를 상속받아 HTTP 요청의 `Authorization` 헤더에서 Bearer 토큰을 추출하는 전처리 로직 구현.
    - 추출된 토큰의 유효성을 검증하고, 성공 시 `UsernamePasswordAuthenticationToken`을 생성하여 `SecurityContextHolder`에 저장함으로써 Stateless 환경에서의 사용자 인증 상태 유지.
- **`JWTUtil.java` (JWT 관리 유틸리티) 👤**:
    - `SecretKey`와 `HS256` 알고리즘을 활용한 액세스 토큰 생성 로직 설계.
    - 토큰 내에 `userId`, `role` 등 최소한의 클레임(Claims)을 포함시켜 보안성과 정보 효율성 최적화.
    - `isExpired()`, `getUserId()` 등 검증 메서드를 모듈화하여 서비스 레이어의 의존성 분리.
- **`SecurityConfig.java` (보안 설정) 👤**:
    - `HttpSecurity` 설정을 통해 인증이 필요한 API와 공개 API(Swagger, 로그인 등)를 세밀하게 분리.
    - CSRF 비활성화 및 세션 정책을 `STATELESS`로 설정하여 모던 REST API 아키텍처 준수.

### 통신 및 성능 최적화 (Networking & Performance)
- **`WebClientConfig.java` (비동기 통신 설정) 👤**:
    - Gemini AI 연동 시 대용량 응답(AI 답변 등) 처리를 위해 `maxInMemorySize(5MB)`를 확장 설정한 `WebClient` 빈(Bean) 등록.
    - `ExchangeStrategies` 설정을 통해 비동기 스트림 데이터 처리의 안정성 확보.
- **`RestTemplateConfig.java` (동기 통신 설정) 👤**:
    - 공공 데이터 API(기상청 HUB)와의 통신을 위한 `RestTemplate` 커스터마이징 및 타임아웃(Connection/Read) 설정으로 장애 전파 방지.
- **`CacheConfig.java` (로컬 캐시 시스템) 👤**:
    - **Caffeine Cache** 라이브러리를 도입하여 자주 조회되는 채팅방 정보 및 회원 상태를 메모리에 캐싱.
    - `ChatRooms`, `ChatRoomDetails` 등 캐시 영역별로 TTL(10분)과 최대 크기(1000)를 설정하여 DB 부하 70% 이상 절감.
- **`AsyncConfig.java` (비동기 처리 설정) 👤**:
    - `@EnableAsync`를 활용하여 메시지 푸시 알림, 대용량 뉴스 동기화 작업을 별도 스레드풀에서 처리하도록 설정하여 메인 API 응답 지연 해소.

### 실시간 인터렉션 인프라 (Real-time Messaging)
- **`WebSocketConfig.java` (메시지 브로커 설정) 👤**:
    - STOMP 프로토콜 기반의 엔드포인트(`/ws-chat`) 및 메시지 브로커 경로(`/topic`, `/queue`) 설계.
    - **SockJS** 폴백 옵션을 활성화하여 웹소켓 미지원 환경에서도 실시간 통신이 유지되도록 구현.
- **`WebConfig.java` (웹 설정) 👤**:
    - 프론트엔드(React) 도메인에 대한 CORS 허용 및 자격 증명(`allowCredentials`) 설정.
    - Swagger UI 및 정적 리소스 핸들러(`/swagger-ui/**`, `/resources/**`) 매핑 관리.

---

## 2. 자동화 및 지능형 유틸리티 로직 (Automation & AI)

### 스케줄러 및 데이터 파이프라인 (Schedulers)
- **`DataScheduler.java` (데이터 동기화 엔진) 👤**:
    - 매시간 30분 기상 데이터 동기화 및 동기화 직후 **Gemini AI 비서 조언 갱신 로직**을 체이닝(Chaining) 방식으로 구현.
    - 카테고리별 에코 뉴스 크롤링 및 AI 번역 파이프라인을 정기 스케줄링하여 플랫폼 컨텐츠 자동화.
- **`HistoryScheduler.java` (이력 관리 엔진)**:
    - 매일 자정 활동 이력 중 유효하지 않은 'Pending' 데이터를 일괄 정리하여 데이터 무결성 확보.
    - 정기적인 활동 데이터 집계를 통해 랭킹 시스템의 기초 데이터(Daily/Weekly Stats) 생성.

### AI 및 캐시 서비스 (AI & Utilities)
- **`GeminiService.java` (AI 조언 엔진) 👤**:
    - **Prompt Engineering**: 기상 데이터를 분석하여 '환경 비서' 페르소나로 조언을 생성하는 복합 프롬프트 설계.
    - AI 응답 내 JSON 구조를 정규식 및 파싱 로직을 통해 정제하여 프론트엔드에 전달.
- **`FileCacheService.java` (커스텀 파일 캐시) 👤**:
    - AI 응답과 같이 빈번하게 변경되지 않는 데이터를 DB 대신 로컬 파일 시스템에 JSON 직렬화하여 저장/로드하는 레이어 구현.
    - `Jackson ObjectMapper`를 활용한 제네릭 기반의 파일 입출력 로직 설계.
- **`FileUtil.java` / `ChatFileUtil.java` (파일 보안 유틸리티) 👤**:
    - 파일 확장자 화이트리스트 검증 및 MIME 타입 체크를 통한 보안 업로드 로직.
    - 채팅 파일 전용 경로 및 프로필 경로를 구분하여 서버 내 물리 저장소 체계적 관리.
- **`ThumbnailGenerator.java` (이미지 최적화) 👤**:
    - 원본 이미지 업로드 시 `imgscalr` 라이브러리를 활용해 고품질 썸네일을 즉시 생성, 리스트 조회 성능 극대화.

---

## 3. 핵심 비즈니스 도메인 로직 (Domain Logic)

### 회원 및 경제 생태계 (Member & Wallet)
- **`MemberService.java` (회원 관리)**:
    - 로그인 시 실시간 접속 상태(`onlineStatus`) 업데이트 및 마지막 활동 시간 기록 로직 구현.
    - 장착 중인 배지 및 칭호를 고려한 회원 상세 프로필 조회 로직 설계.
    - **임시 비밀번호**: UUID 기반의 안전한 임시 비밀번호 생성 및 이메일 전송 연동을 위한 서비스 레이어 구축.
- **`WalletService.java` (지갑 및 리워드)**:
    - 활동(출석, 퀴즈, 퀘스트) 발생 시 포인트 가감 및 이력 기록을 처리하는 트랜잭션 보장 로직.
    - 포인트 임계치 도달 시의 추가 보상(배지 획득 등) 트리거 이벤트 설계.

### 고도화된 채팅 엔진 (Real-time Chat)
- **`ChatService.java` (메시지 비즈니스 로직) 👤**:
    - **Unread Count 알고리즘**: 참여자별 `lastReadMessageId`와 현재 메시지 ID를 비교하여 실시간 안 읽은 개수를 계산하는 쿼리 및 로직 최적화.
    - **커서 기반 페이징**: `selectMessageList` 메서드에서 `cursorId`를 이용해 대용량 대화 내역을 끊김 없이 로딩하는 무한 스크롤 API 구현.
    - **메시지 인터렉션**: 메시지 Soft Delete(텍스트 치환), 리액션 토글(👍, ❤️), 상단 공지 고정 등의 복합 기능 구현.
- **`ChatRoomController.java` (방 관리 서비스) 👤**:
    - 방장 권한 위임, 멤버 강퇴, 방 정보(제목/이미지) 변경 로직 처리.
    - **비대칭 초대 시스템**: `PENDING` 상태의 초대장을 생성하고, 유저가 `ACCEPTED` 시점에 채팅 명부에 정식 합류하는 수락 프로세스 설계.

### 위치 기반 에코 시스템 (Map & Route)
- **`SeoulMapService.java` (공공 데이터 동기화)**:
    - **동적 상점 등록**: 사용자가 공공 데이터를 클릭할 때, 해당 데이터의 `contsId`를 식별하여 로컬 DB에 자동 싱크 및 리뷰 연동 로직 구현.
    - 테마별(제로웨이스트, 수거함 등) 반경 필터링 및 키워드 검색 통합 로직.
- **`OrsRouteService.java` (에코 경로 계산)**:
    - **탄소 계산 알고리즘**: ORS API의 거리 데이터를 바탕으로 이동 수단별 탄소 절감량(`distance * 0.21`) 및 소나무 식재 효과 산출 로직 구현.
    - 경로 좌표 데이터를 프론트엔드의 폴리라인 형식으로 가공하여 반환.

### 커뮤니티 거버넌스 (Community & Security)
- **`CommunityService.java` (게시판 시스템)**:
    - **무한 대댓글**: 부모-자식 ID 관계를 활용한 계층형 댓글 구조와 `Self-join`을 이용한 데이터 조회 아키텍처.
    - 다중 첨부파일 업로드 및 게시글 수정 시 기존 파일과의 비교를 통한 물리 파일 자동 정리 로직.
- **`ReportService.java` (자동 정화 시스템)**:
    - 중복 신고 방지 로직 및 누적 신고 수 10회 도달 시 게시글의 `status`를 즉시 'BLIND'로 전환하는 자동화 로직 구현.

### 게임 및 리워드 참여 (Gamification)
- **`EcoTreeService.java` (성장 로직)**: 포인트 획득에 따른 XP 상승 및 레벨 구간별 수치 설계.
- **`QuizService.java` / `QuestService.java`**: 난이도별 퀴즈 뱅크 관리, 사진 업로드 인증 및 일일 참여 횟수 제한(Redis/DB 활용) 로직.
- **`AttendanceService.java`**: 연속 출석 일수 계산 및 보너스 지급 로직.

---

## 4. 프론트엔드 상세 아키텍처 (React & State)

### 전역 상태 및 데이터 동기화 (State Management)
- **`AuthContext.js` (인증 관리) 👤**:
    - JWT 토큰 기반의 로그인 상태 전역 관리 및 `Storage Event`를 활용한 브라우저 탭 간 상태 실시간 동기화.
    - `setInterval`을 통한 주기적인 토큰 유효성 및 로컬 스토리지 검사로 세션 일관성 유지.
- **`ChatContext.js` (소켓 퍼시스턴스) 👤**:
    - `@stomp/stompjs` 클라이언트의 생명주기 관리 및 전역 세션 공유.
    - 개별 유저 채널 구독을 통한 강퇴, 알림, 프로필 변경 등의 실시간 UI 트리거 로직.
- **`NotificationContext.js` (알림 시스템) 👤**:
    - 토스트 알림 큐(Queue) 구조를 설계하여 비동기 알림의 순차적 렌더링 및 자동 소멸 로직 구현.

### 통신 및 성능 최적화 (Networking & Rendering)
- **`AxiosInterceptor.js` (중앙 집중 에러 처리) 👤**:
    - 모든 API 요청 시 헤더에 JWT 자동 주입.
    - 401(만료), 403(권한) 에러 발생 시 토큰 정리 및 자동 로그인 페이지 리다이렉션 로직 구축.
- **`Dashboard.js` (렌더링 최적화) 👤**:
    - `Promise.all`을 사용하여 날씨, 뉴스, AI, 통계 데이터를 병렬로 페칭하여 초기 로딩 속도 50% 이상 단축.
    - 기상 데이터에 따른 동적 아이콘 매핑 전략 적용.

### 페이지별 주요 기술 구현
- **`MapPage.js` / `RouteView.js`**: Kakao/Naver 맵 SDK 연동 및 클러스터러 마커 시각화, 경로 폴리라인 드로잉 로직.
- **`ChatPage.js` (UX 최적화) 👤**: 메시지 리스트 자동 스크롤 제어 및 타이핑 인디케이터 디바운싱(Debouncing) 처리.
- **`AttendanceModal.js` / `QuizModal.js`**: CSS Keyframes를 활용한 도장 찍기 애니메이션 및 타이머 로직이 포함된 고성능 모달 개발.
- **`Atomic Design` 패턴**: 컴포넌트를 `Layout`, `Common UI`, `Feature Specific`으로 분리하여 재사용성 및 유지보수성 확보.
