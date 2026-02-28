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
public class UserItemList {
	
	
	private int itemId;
	private String name;
	private String description;
	private int price;
	private String rarity;
	private String isOnSale;
	private String category;
	private Date createdAt;
	private Date updatedAt;
	
	private int uiId;
	private int userId;
	private Date acquiredAt;
	private String isEquipped;
	private Date equippedAt;
	
}
