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
public class CommunityPostLikesVO {

//	PL_ID	NUMBER	No		1	게시글 좋아요 ID (PK)
//	POST_ID	NUMBER	No		2	대상 게시글 ID (FK)
//	MEMBER_ID	NUMBER	No		3	좋아요 누른 회원 ID (FK)
//	CREATED_AT	DATE	No	SYSDATE 	4	누른 시간
//	STATUS	VARCHAR2(1 BYTE)	No	'Y' 	5	좋아요 상태 (Y: 선택, N: 취소)
	
	private int plId;
	private int postId;
	private int memberId;
	private Date createdAt;
	private String status;
	
}
