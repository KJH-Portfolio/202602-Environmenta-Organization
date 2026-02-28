package com.kh.spring.member.model.vo;

import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MemberVO {

//	MEMBER_ID	NUMBER	No		1	회원 고유 ID (PK)
//	LOGIN_ID	VARCHAR2(50 BYTE)	No		2	로그인 아이디
//	PASSWORD	VARCHAR2(100 BYTE)	No		3	비밀번호 (암호화 저장)
//	NAME	VARCHAR2(30 BYTE)	No		4	회원 이름
//	BIRTHDAY	VARCHAR2(10 BYTE)	Yes		5	생년월일
//	GENDER	VARCHAR2(10 BYTE)	Yes		6	성별
//	ADDRESS	VARCHAR2(200 BYTE)	Yes		7	주소
//	STATUS	VARCHAR2(20 BYTE)	Yes		8	회원 상태 (ACTIVE, INACTIVE, BLOCK 등)
//	QUIZ_ATTEMPT_COUNT	NUMBER	Yes		9	퀴즈 시도 횟수
//	QUIZ_CORRECT_COUNT	NUMBER	Yes		10	퀴즈 정답 횟수
//	CREATED_AT	DATE	No	SYSDATE 	9	생성 일시
//	UPDATE_AT	DATE	No	SYSDATE 	10	수정 일시

	private int memberId;
	private String loginId;
	private String password;
	private String name;
	private String birthday;
	private String gender;
	private String address;
	private String status;
	private int quizAttemptCount;
	private int quizCorrectCount;
	private Date createdAt;
	private Date updateAt;
	private String statusMessage;
	private String profileImageUrl;

}
