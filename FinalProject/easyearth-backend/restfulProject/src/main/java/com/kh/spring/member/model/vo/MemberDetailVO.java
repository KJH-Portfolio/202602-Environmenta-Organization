package com.kh.spring.member.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MemberDetailVO {
    private int memberId;
    private String loginId;
    private String name;
    private int quizCorrectCount;
    private int quizAttemptCount;
    private String birthday;
    private String gender;
    private String address;

    private String createdAt;
    private String updateAt;
    private int isOnline;
    private String statusMessage;
    private int reviewCount;
}