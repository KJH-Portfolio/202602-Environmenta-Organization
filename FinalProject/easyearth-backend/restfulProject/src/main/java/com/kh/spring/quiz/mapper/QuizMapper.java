package com.kh.spring.quiz.mapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QuizMapper {
        java.util.List<com.kh.spring.quiz.model.vo.Quiz> selectQuizListByDifficulty(String difficulty);

        int countTodayAttempts(@org.apache.ibatis.annotations.Param("userId") int userId,
                        @org.apache.ibatis.annotations.Param("difficulty") String difficulty);

        int insertQuizHistory(@org.apache.ibatis.annotations.Param("userId") int userId,
                        @org.apache.ibatis.annotations.Param("quizNo") int quizNo,
                        @org.apache.ibatis.annotations.Param("correctYn") String correctYn);

        int updateMemberPoints(@org.apache.ibatis.annotations.Param("userId") int userId,
                        @org.apache.ibatis.annotations.Param("points") int points);

        // 퀴즈 번호로 개별 퀴즈 조회
        com.kh.spring.quiz.model.vo.Quiz selectQuizByNo(@org.apache.ibatis.annotations.Param("quizNo") int quizNo);
}
