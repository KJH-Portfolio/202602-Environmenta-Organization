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
public class CommunityReplyLikesVO {
	
//	RL_ID	NUMBER	No		1	댓글 좋아요 ID (PK)
//	REPLY_ID	NUMBER	No		2	대상 댓글 ID (FK)
//	POST_ID	NUMBER	No		3	대상 게시글 ID (FK)
//	MEMBER_ID	NUMBER	No		4	좋아요 누른 회원 ID (FK)
//	CREATED_AT	DATE	No	SYSDATE 	5	누른 시간
//	STATUS	VARCHAR2(1 BYTE)	No	'Y' 	5	좋아요 상태 (Y: 선택, N: 취소)
	
	private int rlId;
	private int replyId;
	private int postId;
	private int memberId;
	private Date createdAt;
	private String status;
}
