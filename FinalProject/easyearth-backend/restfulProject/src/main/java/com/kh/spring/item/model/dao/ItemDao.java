package com.kh.spring.item.model.dao;

import java.util.HashMap;
import java.util.List;

import com.kh.spring.item.model.vo.RandomPullHistory;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import com.kh.spring.item.model.vo.ItemVO;
import com.kh.spring.item.model.vo.UserItemList;
import com.kh.spring.item.model.vo.UserItemsVO;

@Repository
public class ItemDao {

	//포인트상점 아이템 조회
	public List<ItemVO> storeItem(SqlSession sqlSession) {
		
		return sqlSession.selectList("itemMapper.storeItem");
	}
	
	//포인트상점 보유중인 아이템 조회
	public List<UserItemList> storeMyItem(SqlSessionTemplate sqlSession,int memberId) {
		
		return sqlSession.selectList("itemMapper.storeMyItem",memberId);
	}
	
	//전체 아이템 중 특정 하나 조회
	public ItemVO itemsDetail(SqlSessionTemplate sqlSession, int itemId) {

		return sqlSession.selectOne("itemMapper.itemsDetail",itemId);
	}

	//보유 아이템 중 특정 하나 조회
	public UserItemList myItemsDetail(SqlSessionTemplate sqlSession, HashMap map) {

		return sqlSession.selectOne("itemMapper.myItemsDetail",map);
	}

	//보유중인 아이템 수 조회
	public int itemCount(SqlSessionTemplate sqlSession, int memberId) {

		return sqlSession.selectOne("itemMapper.itemCount",memberId);
	}

	//카테고별 아이템 조회
	public List<ItemVO> itemCategories(SqlSessionTemplate sqlSession, String category) {
		
		return sqlSession.selectList("itemMapper.itemCategories",category);
	}
	
	//등급별 아이템 조회
	public List<ItemVO> itemRarity(SqlSessionTemplate sqlSession, String rarity) {
		
		return sqlSession.selectList("itemMapper.itemRarity",rarity);
	}
	
	//상품 구매시 포인트 차감
	public int deductItemPoint(SqlSessionTemplate sqlSession, HashMap map) {
		
		return sqlSession.insert("itemMapper.deductItemPoint",map);
	}
	
	//포인트상점 아이템 구매
	public int buyItem(SqlSessionTemplate sqlSession, UserItemsVO userItemsVO) {
		
		return sqlSession.insert("itemMapper.buyItem",userItemsVO);
	}
	
	
	//아이템 장착/해제
	public String selectCategoryByUiId(SqlSessionTemplate sqlSession,int userId, int itemId) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("itemId", itemId);
        return sqlSession.selectOne("itemMapper.selectCategoryByUiId", map);
    }

    public int unequipByCategory(SqlSessionTemplate sqlSession,int userId, String category) {
    	HashMap<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("category", category);
        return sqlSession.update("itemMapper.unequipByCategory", map);
    }

    public int equipItem(SqlSessionTemplate sqlSession,int userId, int itemId) {
    	HashMap<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("itemId", itemId);
        return sqlSession.update("itemMapper.equipItem", map);
    }
    
    public String selectStatus(SqlSessionTemplate sqlSession , int userId, int itemId) {
    	HashMap<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("itemId", itemId);
    	return sqlSession.selectOne("itemMapper.selectStatus", map);
    }
    
    
    //랜덤뽑기
	public ItemVO randomPull(SqlSessionTemplate sqlSession, String rarity){
		return sqlSession.selectOne("itemMapper.randomPull", rarity);
	}

	//아이템을 MEMBER 테이블에 INSERT
	public int insertItemToMember(SqlSessionTemplate sqlSession, RandomPullHistory randomPullHistory) {
		return sqlSession.insert("itemMapper.insertItemToMember", randomPullHistory);
	}
	// 중복 확인
	public int checkDuplicateItem(SqlSessionTemplate sqlSession, RandomPullHistory history) {
		return sqlSession.selectOne("itemMapper.checkDuplicateItem", history);
	}

	// 포인트 지급
	public int addPoint(SqlSessionTemplate sqlSession, int memberId) {
		return sqlSession.update("itemMapper.addPoint", memberId);
	}
	// 보유 포인트에서 1000 차감
	public int deductPoint(SqlSessionTemplate sqlSession, int memberId) {
		return sqlSession.update("itemMapper.deductPoint",memberId);
	}
	public int updateStatus(SqlSessionTemplate sqlSession, int userId, int itemId) {
		HashMap<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("itemId", itemId);
		return sqlSession.update("itemMapper.updateStatus",map);
	}

	
}
