package com.kh.spring.quest.model.vo;

import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quest {
    private int questNo;
    private String questTitle;
    private int point;
    private String category;
    private Date createdAt;
    // 오늘 해당 퀘스트 인증 여부
    private boolean completed;
}
