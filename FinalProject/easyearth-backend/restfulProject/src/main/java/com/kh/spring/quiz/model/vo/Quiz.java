package com.kh.spring.quiz.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quiz {
    private int quizNo;
    private String quizQuestion;
    private int quizAnswer; // 정답 번호 (1~4)
    private String quizExplanation;
    private int point;

    // 추가된 필드
    private String difficulty; // Easy, Normal, Hard
    private String option1;
    private String option2;
    private String option3;
    private String option4;
}
