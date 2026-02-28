package com.kh.spring.staticCache.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class StaticCacheVO {
    private int memberId;
    private float co2;
    private float tree;
    private int quizSuccessCount;
    private int quizFailCount;
    private double quizRate;
}
