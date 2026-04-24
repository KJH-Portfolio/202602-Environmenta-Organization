# 📋 EasyEarth 파이널 프로젝트 API Specification

> **프론트엔드-백엔드 간 인터페이스 명세 및 통신 규격**  
> 이 문서는 파이널 프로젝트의 핵심 비즈니스 로직을 수행하는 주요 API들의 엔드포인트, 요청 파라미터, 응답 스키마를 정의합니다.

---

## 📑 목차
1. [인증 및 보안 (Auth/Security)](#1-인증-및-보안-authsecurity)
2. [탄소 계산 및 경로 (Map/Route)](#2-탄소-계산-및-경로-maproute)
3. [AI 환경 비서 (Gemini/AI)](#3-ai-환경-비서-geminiai)
4. [실시간 메시징 (WebSocket/Chat)](#4-실시간-메시징-websocketchat)

---

## 💡 API 설계 원칙 (Technical Note)
- **RESTful 성숙도**: 리소스 기반의 URL 설계와 HTTP Method(GET, POST, PUT, DELETE)를 명확히 구분하여 사용합니다.
- **데이터 형식**: 모든 통신 데이터는 **JSON(UTF-8)** 형식을 표준으로 하며, 에러 응답 시 통일된 에러 객체를 반환합니다.
- **보안**: `/api/**` 하위의 모든 요청은 헤더에 유효한 **JWT(Bearer Token)**를 포함해야 합니다.

---

## 🔐 1. 인증 및 보안 (Auth/Security)

### 1.1 사용자 로그인 및 JWT 발급
- **Endpoint**: `POST /auth/login`
- **Description**: 사용자 인증을 수행하고 Access/Refresh 토큰을 발급합니다.

**Request Body:**
```json
{
  "userId": "user01",
  "userPwd": "password123"
}
```

**Response Body:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "dGhpcy1pcy1yZWZyZXNo...",
  "userInfo": {
    "nickname": "에코전사",
    "role": "MEMBER"
  }
}
```

---

## 🌏 2. 탄소 계산 및 경로 (Map/Route)

### 2.1 ORS 기반 탄소 절감량 계산
- **Endpoint**: `GET /api/route/ors`
- **Description**: 출발지와 목적지를 기반으로 이동 수단별 탄소 절감 수치를 반환합니다.

**Query Parameters:**
| Parameter | Type | Default | Description |
|---|---|---|---|
| `startX` | Double | 126.974 | 출발지 경도 |
| `startY` | Double | 37.564 | 출발지 위도 |
| `mode` | String | driving-car | 이동 수단 (driving-car, foot-walking, cycling-regular) |

**Response Body:**
```json
{
  "distance": 15.2,
  "duration": 1800,
  "carbonSaving": 3.192,
  "treeEffect": 0.45,
  "routePoints": [[126.9, 37.5], ...]
}
```

---

## 🤖 3. AI 환경 비서 (Gemini/AI)

### 3.1 환경 비서 맞춤형 조언 생성
- **Endpoint**: `POST /gemini/secretary`
- **Description**: 현재 날씨 및 사용자 활동 데이터를 기반으로 AI 환경 조언을 생성합니다.

**Response Body:**
```json
{
  "message": "현재 서울은 미세먼지가 높습니다. 외출 시에는 대중교통을 이용하고, 실내에서는 공기정화 식물을 가꾸는 것을 추천합니다."
}
```

---

## 💬 4. 실시간 메시징 (WebSocket/Chat)

### 4.1 WebSocket 엔드포인트 및 목적지
- **Socket Endpoint**: `ws://localhost:8080/ws-stomp`
- **Pub/Sub Path**:
    - **Subscribe**: `/topic/chat/room/{roomId}` (메시지 수신)
    - **Publish**: `/app/chat/message` (메시지 전송)

**Message Schema (JSON):**
```json
{
  "chatRoomId": 101,
  "senderId": "user01",
  "content": "안녕하세요! 분리수거 관련 질문 있습니다.",
  "messageType": "TALK"
}
```

---

## 🚀 5. Error Code Definition

| Status | Code | Description |
|---|---|---|
| 401 | `UNAUTHORIZED` | JWT 토큰이 없거나 만료됨 |
| 403 | `FORBIDDEN` | 해당 리소스에 대한 접근 권한 없음 |
| 500 | `INTERNAL_SERVER_ERROR` | AI 서버 또는 외부 API 통신 장애 |
