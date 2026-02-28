// =========================================================================
// [채팅 예외(에러) 처리 전담반 - 개발 중 충돌을 피하기 위해 현재 임시 주석 처리됨]
// 백엔드 실행 중 어디선가 에러가 뻥 터졌을 때(Exception), 브라우저의 하얀 기본 에러 페이지 대신
// 프론트엔드가 알아먹기 좋은 예쁜 JSON 포맷으로 "이유"를 포장해주는 전역 에러 핸들러 클래스입니다.
// (1. IllegalArgumentException: "없는 방입니다", "방장이 아닙니다" 등 우리가 고의로 던진 에러 처리)
// (2. OptimisticLockException: 동시에 같은 작업을 요청했을 때 DB 충돌 방지 예외 처리)
// (3. Exception: 그 외 모든 알 수 없는 500 서버 폭발 에러 처리)
// =========================================================================

// package com.kh.spring.chat.exception;

// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.ExceptionHandler;
// import org.springframework.web.bind.annotation.RestControllerAdvice;

// import lombok.extern.slf4j.Slf4j;

// @Slf4j
// @RestControllerAdvice(basePackages = "com.kh.spring.chat")
// public class ChatExceptionHandler {

//     /**
//      * IllegalArgumentException 처리 (잘못된 요청)
//      */
//     @ExceptionHandler(IllegalArgumentException.class)
//     public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
//         log.warn("IllegalArgumentException: {}", e.getMessage());
//         ErrorResponse errorResponse = new ErrorResponse("BAD_REQUEST", e.getMessage());
//         return ResponseEntity.badRequest().body(errorResponse);
//     }

//     /**
//      * AccessDeniedException 처리 (권한 없음)
//      */
//     @ExceptionHandler(IllegalAccessException.class)
//     public ResponseEntity<ErrorResponse> handleAccessDenied(IllegalAccessException e) {
//         log.warn("AccessDeniedException: {}", e.getMessage());
//         ErrorResponse errorResponse = new ErrorResponse("FORBIDDEN", e.getMessage());
//         return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
//     }

//     /**
//      * OptimisticLockException 처리 (동시성 충돌)
//      */
//     @ExceptionHandler(jakarta.persistence.OptimisticLockException.class)
//     public ResponseEntity<ErrorResponse> handleOptimisticLock(jakarta.persistence.OptimisticLockException e) {
//         log.warn("OptimisticLockException: {}", e.getMessage());
//         ErrorResponse errorResponse = new ErrorResponse("CONFLICT", "다른 사용자가 동시에 수정했습니다. 다시 시도해주세요.");
//         return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
//     }

//     /**
//      * 기타 모든 예외 처리
//      */
//     @ExceptionHandler(Exception.class)
//     public ResponseEntity<ErrorResponse> handleGenericException(Exception e) {
//         log.error("Unexpected exception occurred", e);
//         ErrorResponse errorResponse = new ErrorResponse("INTERNAL_SERVER_ERROR", "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
//         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
//     }

//     /**
//      * 에러 응답 DTO
//      */
//     public static class ErrorResponse {
//         private String code;
//         private String message;

//         public ErrorResponse(String code, String message) {
//             this.code = code;
//             this.message = message;
//         }

//         public String getCode() {
//             return code;
//         }

//         public String getMessage() {
//             return message;
//         }
//     }
// }
