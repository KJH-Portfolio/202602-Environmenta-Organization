package com.kh.spring.item.controller;


import java.util.HashMap;
import java.util.List;

import com.kh.spring.item.model.vo.RandomPullHistory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kh.spring.item.model.service.ItemService;
import com.kh.spring.item.model.vo.ItemVO;
import com.kh.spring.item.model.vo.UserItemList;
import com.kh.spring.item.model.vo.UserItemsVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/items")
@Slf4j
@Tag(name = "아이템 컨트롤러", description = "아이템 관련 전부")
public class ItemController {
	
	@Autowired
	private ItemService service;
	
	//포인트상점 아이템 조회
	@Operation(summary = "상점 아이템 조회", description = "상점 아이템 조회")
	@GetMapping("/select")
	public ResponseEntity<?> storeItem(){
		
		List<ItemVO> list = service.storeItem();
		
		return ResponseEntity.ok(list);
	}
	
	//포인트상점 보유중인 아이템 조회
	@Operation(summary = "보유중인 아이템 조회", description = "보유중인 아이템 조회")
	@GetMapping("/myItems/{memberId}")
	public ResponseEntity<?> storeMyItem(@PathVariable int memberId){
		
		List<UserItemList> list = service.storeMyItem(memberId);
		
		return ResponseEntity.ok(list);
	}
	
	//전체 아이템 중 특정 하나 조회
	@Operation(summary = "전체 아이템 중 특정 하나 조회", description = "전체 아이템 중 특정 하나 조회")
	@GetMapping("/itemsDetail/{itemId}")
	public ResponseEntity<?> itemsDetail(@PathVariable int itemId){
		
		ItemVO list = service.itemsDetail(itemId);
		
		return ResponseEntity.ok(list);
	}
	
	//보유 아이템 중 특정 하나 조회
	@Operation(summary = "보유중인 아이템 상세조회", description = "보유중인 아이템 상세조회")
	@GetMapping("/myItemsDetail/{itemId}")
	public ResponseEntity<?> myItemsDetail(@PathVariable int itemId, @RequestParam int userId){
		
		HashMap<String, Object> map = new HashMap<>();
		map.put("itemId", itemId);
		map.put("userId", userId);
		
		UserItemList list = service.myItemsDetail(map);
		
		return ResponseEntity.ok(list);
	}
	
	//보유 아이템 수 조회
	@Operation(summary = "보유 아이템 수 조회", description = "보유 아이템 수 조회")
	@GetMapping("/itemCount/{memberId}")
	public ResponseEntity<?> itemCount(@PathVariable int memberId){
		
		int count = service.itemCount(memberId);
		
		return ResponseEntity.ok(count);
		
	}
	
	//포인트상점 아이템 카테고리 조회
	@Operation(summary = "상점 카테고리 조회", description = "상점 카테고리 조회")
	@GetMapping("/categories/{category}")
	public ResponseEntity<?> categories(@PathVariable String category) {
		
		List<ItemVO> list = service.itemCategories(category);
		
		return ResponseEntity.ok(list);
	}
	
	//포인트상점 아이템 등급별 조회
	@Operation(summary = "상점 등급별 조회", description = "상점 등급별 조회")
	@GetMapping("/rarity/{rarity}")
	public ResponseEntity<?> itemRarity(@PathVariable String rarity) {
		
		List<ItemVO> list = service.itemRarity(rarity);
		
		return ResponseEntity.ok(list);
	}
	
	
	//포인트상점 아이템 구매
	@Operation(summary = "상점 아이템 구매", description = "상점 아이템 구매")
	@PostMapping("/buy")
	public ResponseEntity<?> buyItem(@RequestBody UserItemsVO userItemsVO){
		System.out.println(userItemsVO.getPrice());
		int result = service.buyItem(userItemsVO);
		
		if(result>0) {
			
			return ResponseEntity.ok("아이템 구매 성공!");
			
		}else {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("포인트가 부족합니다.");
		}
		
	}
	
	// 아이템 장착
	@Operation(summary = "아이템 장착/해제", description = "아이템 장착/해제")
	@PatchMapping("/{itemId}/equip")
	public ResponseEntity<?> equipItem(
			@PathVariable int itemId,
			@RequestParam int userId) {

		int result = service.equipItem(userId, itemId);

		// 1. 장착 성공
		if(result > 0) {
			return ResponseEntity.ok("아이템 장착 완료");
		}
		// 2. 해제 성공
		else if(result == -1) {
			return ResponseEntity.ok("아이템 장착 해제 완료");
		}
		// 3. 아이템 없음 (401보다는 404가 적절합니다)
		else if(result == -2) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("아이템이 존재하지 않습니다.");
		}
		// 4. 기타 실패 (서버 오류 등)
		else {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("아이템 장착 실패");
		}
	}


	@GetMapping("/random/{memberId}")
	@ResponseBody
	@Operation(summary = "랜덤뽑기 API", description = "랜덤뽑기 API")
	public ResponseEntity<?> randomPull(@PathVariable int memberId) {
		RandomPullHistory randomPullHistory = new RandomPullHistory();
		int randomNum = (int) (Math.random() * 100) + 1;
		//1~69 : COMMON 69%
		//70~94 : RARE  25%
		//95~99 : EPIC 5%
		//100 : LEGENDARY 1%
		if (randomNum <= 25) randomPullHistory.setRarity("COMMON");
		else if (randomNum <= 50) randomPullHistory.setRarity("RARE");
		else if (randomNum <= 75) randomPullHistory.setRarity("EPIC");
		else randomPullHistory.setRarity("LEGENDARY");
		randomPullHistory.setMemberId(memberId);

		int result = service.randomPull(randomPullHistory);
		if(result == 2) {
			return ResponseEntity.ok("중복 아이템 당첨.. 500포인트가 환급되었습니다.");
		}
		else if(result==-1) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("포인트가 부족합니다. (최소 1000P 필요)");
		}
		else if(result > 0) {
			return ResponseEntity.ok(randomPullHistory);
		}
		else {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("랜덤뽑기 오류 발생");
		}
	}

}
