package com.kh.spring.report.model.vo;

import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ReportsVO {

//	REPORTS_ID	NUMBER	No		1	신고 고유 ID (PK)
//	REPLY_ID	NUMBER	Yes		2	신고된 댓글 ID (댓글 신고 시)
//	POST_ID	NUMBER	Yes		3	신고된 게시글 ID (게시글 신고 시)
//	MEMBER_ID	NUMBER	No		4	신고자 회원 ID (FK)
//	TYPE	VARCHAR2(20 BYTE)	No		5	신고 대상 유형 (POST, REPLY)
//	REASON	VARCHAR2(100 BYTE)	No		6	신고 사유 카테고리
//	DETAIL	CLOB	Yes		7	상세 신고 내용
//	STATUS	VARCHAR2(30 BYTE)	No		8	처리 상태 (RECEIVED, RESOLVED, REJECTED)
//	CREATED_AT	DATE	No	SYSDATE 	9	신고 일시
//	RESOLVED_AT	DATE	Yes		10	관리자 처리 일시
//	TARGET_MEMBER_ID	NUMBER	No		11	피신고자 아이디
//	REVIEW_ID	NUMBER	Yes		12	신고된 리뷰 ID (상점 리뷰 신고 시)
	
	private int reportsId;
	private int replyId;
	private int postId;
	private int memberId;
	private String type;
	private String reason;
	private String detail;
	private String status;
	private Date createdAt;
	private Date resolvedAt;
	private int targetMemberId;
	private int reviewId;
	
	private String memberName;      // 신고자 이름
	private String targetMemberName;   // 신고 대상 이름
}

