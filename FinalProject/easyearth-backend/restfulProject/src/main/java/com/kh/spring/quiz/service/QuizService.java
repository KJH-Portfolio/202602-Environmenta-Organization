package com.kh.spring.quiz.service;

import com.kh.spring.quiz.mapper.QuizMapper;
import com.kh.spring.quiz.model.vo.Quiz;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.spring.common.model.vo.IntegratedHistory; // Import 추가

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizService {

        private final QuizMapper quizMapper;
        private final com.kh.spring.common.mapper.HistoryMapper historyMapper;

        /**
         * 난이도별 퀴즈 조회 (INTEGRATED_HISTORY 기반)
         * 오늘 할당이 없으면 난이도별 5문제씩 할당 후 반환
         */
        public List<Quiz> getQuizByDifficulty(String difficulty, int userId) {
                if (userId == 0)
                        return Collections.emptyList();

                // 오늘 할당된 퀴즈 확인
                int assignedCount = historyMapper.countTodayAssignment(userId, "QUIZ");

                // 없으면 새로 할당
                if (assignedCount == 0) {
                        assignTodaysQuizzes(userId);
                }

                // 난이도별 타입 결정
                String type = "QUIZ_" + (difficulty.toUpperCase().startsWith("E") ? "E"
                                : difficulty.toUpperCase().startsWith("N") ? "N" : "H");

                return historyMapper.selectPendingQuizzes(userId, type);
        }

        // 오늘의 난이도별 퀴즈 가능 여부 조회
        public Map<String, Boolean> getDailyQuizStatus(int userId) {
                Map<String, Boolean> status = new HashMap<>();

                if (userId == 0)
                        return status;

                // 1. 할당 여부 확인 및 할당
                int assignedCount = historyMapper.countTodayAssignment(userId, "QUIZ");
                if (assignedCount == 0) {
                        assignTodaysQuizzes(userId);
                }

                // 2. 각 난이도별 남은 문제 수 확인
                int easyCount = historyMapper.countPendingQuizzes(userId, "QUIZ_E");
                int normalCount = historyMapper.countPendingQuizzes(userId, "QUIZ_N");
                int hardCount = historyMapper.countPendingQuizzes(userId, "QUIZ_H");

                status.put("Easy", easyCount > 0);
                status.put("Normal", normalCount > 0);
                status.put("Hard", hardCount > 0);

                return status;
        }

        // 오늘의 퀴즈 할당 (난이도별 5문제)
        private void assignTodaysQuizzes(int userId) {
                String[] diffs = { "Easy", "Normal", "Hard" };
                for (String d : diffs) {
                        List<Quiz> all = quizMapper.selectQuizListByDifficulty(d);
                        if (all != null && !all.isEmpty()) {
                                Collections.shuffle(all);
                                List<Quiz> picked = all.stream().limit(5).collect(Collectors.toList());
                                String type = "QUIZ_" + d.toUpperCase().substring(0, 1);
                                for (Quiz q : picked) {
                                        IntegratedHistory h = IntegratedHistory // 타입명 간소화
                                                        .builder()
                                                        .memberId(userId)
                                                        .activityType(type)
                                                        .refId(q.getQuizNo())
                                                        .build();
                                        historyMapper.insertHistory(h);
                                }
                        }
                }
        }

        /**
         * 퀴즈 풀이 결과 저장 및 포인트 지급
         */
        @Transactional
        public void saveQuizAttempt(int userId, int quizNo, boolean isCorrect, int point) {
                if (userId <= 0) {
                        return;
                }

                // 퀴즈 정보 조회하여 난이도 확인
                Quiz quiz = quizMapper.selectQuizByNo(quizNo);
                if (quiz == null) {
                        return;
                }

                // 난이도 기반 타입 결정
                String diff = quiz.getDifficulty();
                String type = "QUIZ_" + (diff.toUpperCase().startsWith("E") ? "E"
                                : diff.toUpperCase().startsWith("N") ? "N" : "H");

                String status = isCorrect ? "Y" : "N";

                // INTEGRATED_HISTORY 상태 업데이트
                historyMapper.updateHistoryStatus(userId, type, quizNo, status, null);

                // 정답일 경우만 포인트 지급
                if (isCorrect && point > 0) {
                        quizMapper.updateMemberPoints(userId, point);
                }
        }
}
