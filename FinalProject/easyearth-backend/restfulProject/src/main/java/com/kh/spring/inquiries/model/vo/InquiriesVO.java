package com.kh.spring.inquiries.model.vo;

import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class InquiriesVO {
	
//	INQUIRIES_ID	NUMBER	No		1	문의 ID (PK)
//	MEMBER_ID	NUMBER	No		2	문의 작성 사용자 ID
//	TITLE	VARCHAR2(255 BYTE)	No		3	문의 제목
//	CONTENT	CLOB	No		4	문의 내용
//	STATUS	VARCHAR2(50 BYTE)	Yes	'SUBMITTED'	5	문의 상태 (SUBMITTED, PROCESSING, COMPLETED)
//	ADMIN_REPLY	CLOB	Yes		6	관리자 답변
//	IS_PUBLIC	CHAR(1 BYTE)	Yes	"'N'
//	        "	7	공개 여부 (Y/N)
//	CREATED_AT	DATE	Yes	SYSDATE	8	생성일시
//	UPDATED_AT	DATE	No	SYSDATE 	9	수정일시
//	RESOLVED_AT	DATE	Yes		10	처리 완료 일시
//	VIEW_COUNT	NUMBER	Yes	"0"	11	조회수
//	IS_FAQ	CHAR(1 BYTE)	Yes	"'N'"	12	자주 묻는 질문 여부 (Y/N)
	
	private int inquiriesId;
	private int memberId;
	private String title;
	private String content;
	private String status;
	private String adminReply;
	private String isPublic;
	private Date createdAt;
	private Date updatedAt;
	private Date resolvedAt;
	private int viewCount;
	private String isFaq;
	
	private String name;   //사용자 이름
}
