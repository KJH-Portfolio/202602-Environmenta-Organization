package com.kh.spring.community.model.vo;

import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CommunityPostVO {
	
//	POST_ID	NUMBER	No		1	게시글 ID (PK)
//	MEMBER_ID	NUMBER	No		2	작성자 회원 ID (FK)
//	TITLE	VARCHAR2(200 BYTE)	No		3	게시글 제목
//	CONTENT	CLOB	No		4	게시글 내용
//	VIEW_COUNT	NUMBER	No	0 	5	조회수
//	LIKE_COUNT	NUMBER	No	0 	6	좋아요 총합
//	COMMENT_COUNT	NUMBER	No	0 	7	댓글 총합
//	HAS_FILES	NUMBER(1,0)	No	0 	8	파일 첨부 여부 (0:없음, 1:있음)
//	ORIGIN_NAME	VARCHAR2(255 BYTE)	Yes		9	파일 원본명
//	CHANGE_NAME	VARCHAR2(255 BYTE)	Yes		10	파일 수정명
//	REPORT_COUNT	NUMBER	No	0 	11	신고 누적 횟수
//	CREATED_AT	DATE	No	SYSDATE 	12	작성 일시
//	UPDATED_AT	DATE	No	SYSDATE 	13	수정 일시
//	CATEGORY	VARCHAR2(30 BYTE)	No		14	게시글 카테고리
	
	private int postId;
	private int memberId;
	private String title;
	private String content;
	private int viewCount;
	private int likeCount;
	private int commentCount;
	private int hasFiles;
	private String originName;
	private String changeName;
	private int reportCount;
	private Date createdAt;
	private Date updatedAt;
	private String category;	
	private String status;
	
	private String name;
}
