package com.kh.spring.common.model.vo;

import java.sql.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntegratedHistory {
    private int historyId;
    private int memberId;
    private String activityType; // QUIZ_E, QUIZ_N, QUIZ_H, QUEST
    private int refId; // QUIZ_NO or QUEST_NO
    private String status; // P, Y, N
    private String imageUrl; // For Quest
    private Date createdAt;
}
