package com.kh.spring.quiz.controller;

import com.kh.spring.quiz.service.QuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
@Tag(name = "퀴즈 API", description = "퀴즈 API")
public class QuizController {

    private final QuizService quizService;

    // 난이도별 퀴즈 조회 (userId 필수)
    @Operation(summary = "퀴즈 조회", description = "난이도별 퀴즈를 조회하는 API(Easy,Normal,Hard). 유저 ID 필요")
    @GetMapping("/{difficulty}")
    public java.util.List<com.kh.spring.quiz.model.vo.Quiz> getQuizByDifficulty(
            @PathVariable("difficulty") String difficulty,
            @RequestParam("userId") int userId) {
        return quizService.getQuizByDifficulty(difficulty, userId);
    }

    // 퀴즈 진행 현황 조회 (버튼 활성화/비활성화용)
    @Operation(summary = "퀴즈 진행 현황 조회", description = "오늘 난이도별 퀴즈 풀이 가능 여부를 반환합니다.")
    @GetMapping("/status")
    public ResponseEntity<Map<String, Boolean>> getQuizStatus(@RequestParam("userId") int userId) {
        return ResponseEntity.ok(quizService.getDailyQuizStatus(userId));
    }

    // 퀴즈 풀이 결과 저장
    @Operation(summary = "퀴즈 풀이 결과 저장", description = "퀴즈 1문제 풀이 결과를 저장하고 정답 시 포인트를 지급합니다.")
    @PostMapping("/attempt")
    public ResponseEntity<String> saveQuizAttempt(
            @RequestParam("userId") int userId,
            @RequestParam("quizNo") int quizNo,
            @RequestParam("isCorrect") boolean isCorrect,
            @RequestParam("point") int point) {
        try {
            quizService.saveQuizAttempt(userId, quizNo, isCorrect, point);
            return ResponseEntity.ok("퀴즈 이력이 저장되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("저장 중 오류 발생: " + e.getMessage());
        }
    }
}
