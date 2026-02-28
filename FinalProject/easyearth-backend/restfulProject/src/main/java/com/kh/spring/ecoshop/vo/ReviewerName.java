package com.kh.spring.ecoshop.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ReviewerName {
    int esrId;
    int shopId;
    String rating;
    String content;
    String name;
    String memberId;
    private String status;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    Date createdAt;
}