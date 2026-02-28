package com.kh.spring.item.model.vo;

import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ItemVO {

//	ITEM_ID	NUMBER	No		1	아이템 ID (PK)
//	NAME	VARCHAR2(100 BYTE)	No		2	아이템 이름
//	DESCRIPTION	CLOB	Yes		3	아이템 설명
//	PRICE	NUMBER	No		4	아이템 가격
//	RARITY	VARCHAR2(30 BYTE)	Yes		5	아이템 희귀도
//	IMAGE_URL	VARCHAR2(255 BYTE)	Yes		6	아이템 이미지 URL
//	IS_ON_SALE	CHAR(1 BYTE)	Yes	"'Y'
//	        "	7	판매 여부 (Y/N)
//	CATEGORY	VARCHAR2(50 BYTE)	Yes		8	아이템 카테고리
//	CREATED_AT	DATE	Yes	SYSDATE	9	생성일시
//	UPDATED_AT	DATE	Yes		10	수정일시
	
	private int itemId;
	private String name;
	private String description;
	private int price;
	private String rarity;
	private String isOnSale;
	private String category;
	private Date createdAt;
	private Date updatedAt;
	
	
}
