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
public class UserItemsVO {

//	UI_ID	NUMBER	No		1	보유 아이템 ID (PK)
//	ITEM_ID	NUMBER	No		2	아이템 ID (FK)
//	USER_ID	NUMBER	No		3	사용자 ID
//	ACQUIRED_AT	DATE	Yes	SYSDATE	4	획득 일시
//	IS_EQUIPPED	CHAR(1 BYTE)	Yes	"'N'
//	        "	5	장착 여부 (Y/N)
//	EQUIPPED_AT	DATE	Yes		6	장착 일시
//	PRICE	NUMBER	No		7	가격
//	CATEGORY	VARCHAR2(30 BYTE)	Yes		8	카테고리
	
	private int uiId;
	private int itemId;
	private int userId;
	private Date acquiredAt;
	private String isEquipped;
	private Date equippedAt;
	private int price;
	private String category;
	
}
