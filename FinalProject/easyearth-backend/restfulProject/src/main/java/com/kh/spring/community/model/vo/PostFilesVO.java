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
public class PostFilesVO {
	
//	FILES_ID	NUMBER	No		1	파일 고유 ID (PK)
//	POST_ID	NUMBER	No		2	연결된 게시글 ID (FK)
//	MEMBER_ID	NUMBER	No		3	업로드 회원 ID (FK)
//	URL	VARCHAR2(1000 BYTE)	No		4	파일 저장 경로/URL
//	ORIGIN_NAME	VARCHAR2(255 BYTE)	Yes		5	파일 원본명
//	CHANGE_NAME	VARCHAR2(255 BYTE)	Yes		6	파일 수정명
//	TYPE	VARCHAR2(100 BYTE)	No		7	파일 확장자/타입
//	FILE_SIZE	NUMBER	No	0 	8	파일 용량(byte)
//	CREATED_AT	DATE	No	SYSDATE 	9	업로드 일시
	
	private int filesId;
	private int postId;
	private int memberId;
	private String url;
	private String originName;
	private String changeName;
	private String type;
	private int fileSize;
	private Date createdAt;
	
}
