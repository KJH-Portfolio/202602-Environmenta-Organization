package com.kh.spring.item.model.service;

import java.util.HashMap;
import java.util.List;

import com.kh.spring.item.model.vo.ItemVO;
import com.kh.spring.item.model.vo.RandomPullHistory;
import com.kh.spring.item.model.vo.UserItemList;
import com.kh.spring.item.model.vo.UserItemsVO;

public interface ItemService {
	
	//포인트상점 아이템 조회
	List<ItemVO> storeItem();
	
	//포인트상점 보유중인 아이템 조회
	List<UserItemList> storeMyItem(int memberId);

	//전체 아이템 중 특정 하나 조회
	ItemVO itemsDetail(int itemId);

	//보유 아이템 중 특정 하나 조회
	UserItemList myItemsDetail(HashMap map);

	//보유중인 아이템 수 조회
	int itemCount(int memberId);

	//카테고별 아이템 조회
	List<ItemVO> itemCategories(String category);
	
	//등급별 아이템 조회
	List<ItemVO> itemRarity(String rarity);

	//포인트상점 아이템 구매
	int buyItem(UserItemsVO vo);
	
	//아이템 장착
	int equipItem(int userId, int itemId);

	//랜덤 뽑기
	int randomPull(RandomPullHistory randomPullHistory);


	
}
