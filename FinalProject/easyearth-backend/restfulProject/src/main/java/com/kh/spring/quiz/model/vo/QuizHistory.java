package com.kh.spring.quiz.model.vo;

import java.sql.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizHistory {
    private int historyId;
    private int memberId;
    private int quizNo;
    private String correctYn;
    private Date createdAt;
}
