package com.kh.spring.inquiries.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kh.spring.common.model.vo.PageInfo;
import com.kh.spring.common.template.Pagination;
import com.kh.spring.inquiries.model.service.InquiriesService;
import com.kh.spring.inquiries.model.vo.InquiriesListDTO;
import com.kh.spring.inquiries.model.vo.InquiriesVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/inquiries")
@Slf4j
@Tag(name = "건의사항", description = "건의사항")
public class InquiriesController {

	@Autowired
	private InquiriesService service;

	//건의글 목록 조회 
	@Operation(summary = "건의글 목록 조회",
			description = "condition : title(제목) / writer(작성자) / content(내용)   \n\n"
						+ "keyword : condition에 대한 검색어   \n\n"
						+ "status(문의 상태) : SUBMITTED(접수 완료) / PROCESSING(진행 중) / COMPLETED(답변 완료)   \n\n")
	@GetMapping("/post/list")
	public ResponseEntity<?> inquiriesList(@RequestParam(value="page", defaultValue = "1") int currentPage,
								           @RequestParam(defaultValue = "10") int size,
								           @RequestParam(required = false) String condition,
								           @RequestParam(required = false) String keyword,
								           @RequestParam(required = false) String status) {
		
		int listCount = 0;
		int boardLimit = 10;
		int pageLimit = size;
		
		HashMap<String, Object> map = new HashMap<>();
		
		if (keyword != null && !keyword.isEmpty()) {
			map.put("keyword", keyword);
			map.put("condition", condition);
			listCount = service.searchListCount(map); // 검색된 개수
			
		}else if (status != null && !status.isEmpty()) {
			map.put("status", status);
			listCount = service.filterListCount(map); // 필터링된 개수
			
		}else {
			listCount = service.listCount();  //전체 개수
		}
		
		PageInfo pi = Pagination.getPageInfo(listCount, currentPage, boardLimit, pageLimit);
		
		ArrayList<InquiriesVO> list;
		
		if (keyword != null && !keyword.isEmpty()) {
			list = service.searchList(map, pi);
		}else if (status != null && !status.isEmpty()) {
			list= service.filterList(map, pi);
		}else {
			list = service.inquiriesList(pi);
		}
		
		return ResponseEntity.ok(InquiriesListDTO.of(list, pi));	
	}

	//건의글 상세보기
	@Operation(summary = "건의글 상세보기", description = "inquiriesId : 조회할 건의글 번호  \n\n"
													+ "memberId : 로그인된 사용자 아이디")
	@GetMapping("/post/detail/{inquiriesId}")
	public ResponseEntity<?> inquiriesDetail(@PathVariable int inquiriesId,
											 @RequestParam (required = false, defaultValue = "0") int memberId) {
		
		int result = service.increaseCount(inquiriesId);
		
		if(result > 0) {
			InquiriesVO inquiry = service.inquiriesDetail(inquiriesId);
			
			if (inquiry != null) {

				String isPublic = inquiry.getIsPublic();
				
				if(isPublic.equals("N")) {
					if(memberId != 1 && inquiry.getMemberId() != memberId) {
						return ResponseEntity.status(HttpStatus.FORBIDDEN)
											 .body("비공개 건의글은 작성자와 관리자만 확인할 수 있습니다.");
					}
				}
				return ResponseEntity.ok(inquiry);
			}else {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
									 .body("존재하지 않는 건의글입니다.");
			}
		}
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
							 .body("건의글 조회 중 오류 발생");
	}
	
	//건의글 등록
	@Operation(summary = "건의글 등록", 
			description = "memberId : 로그인된 사용자 아이디 \n\n"
						+ "title : 건의글 제목 \n\n"
						+ "content : 건의 내용 \n\n"
						+ "isPublic(전체 공개 여부) : Y / N  \n\n"
						+ "isFaq(자주 묻는 질문) : Y / N  \n\n")
	@PostMapping("/post/insert")
	public ResponseEntity<?> inquiriesInsert(@RequestParam int memberId,
											 @RequestParam String title,
											 @RequestParam String content,
											 @RequestParam String isPublic,
											 @RequestParam(required = false, defaultValue = "N") String isFaq) {
		
		Map<String, Object> map = new HashMap<>();
		map.put("memberId", memberId);
		map.put("title", title);
		map.put("content", content);
		map.put("isPublic", isPublic);
		map.put("isFaq", isFaq);
		
		int result = service.inquiriesInsert(map);
		
		if (result > 0) {
			return ResponseEntity.ok("건의글 등록 성공");
		}else {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
								 .body("건의글 등록 중 오류 발생");
		}
	}
	
	//건의글 수정
	@Operation(summary = "건의글 수정", 
				description = "inquiriesId : 수정할 건의글 번호 \n\n"
							+ "memberId : 로그인된 사용자 아이디  \n\n"
							+ "title : 건의글 제목  \n\n"
							+ "content : 건의 내용  \n\n"
							+ "isPublic(전체 공개 여부) : Y / N  \n\n"
							+ "isFaq(자주 묻는 질문) : Y / N  \n\n"
							+ "		-> 수정할 건의글 작성자와 로그인된 사용자가 동일해야 함")
	@PutMapping("/post/update/{inquiriesId}")
	public ResponseEntity<?> inquiriesUpdate(@PathVariable int inquiriesId,
											 @RequestParam int memberId,
											 @RequestParam String title,
											 @RequestParam String content,
											 @RequestParam String isPublic,
											 @RequestParam(required = false, defaultValue = "N") String isFaq) {
		
		Map<String, Object> map = new HashMap<>();
		map.put("inquiriesId", inquiriesId);
		map.put("memberId", memberId);
		map.put("title", title);
		map.put("content", content);
		map.put("isPublic", isPublic);
		map.put("isFaq", isFaq);
		
		int result = service.inquiriesUpdate(map);
		
		if (result > 0) {
			return ResponseEntity.ok("건의글 수정 성공");
		}else {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
								 .body("건의글 수정 처리 중 오류 발생");
		}
	}

	//건의글 삭제
	@Operation(summary = "건의글 삭제", 
				description = "inquiriesId : 삭제할 건의글 번호  \n\n"
							+ "memberId : 로그인된 사용자 아이디   \n\n"
							+ "		-> 삭제할 건의글 작성자와 로그인된 사용자가 동일해야 함")
	@DeleteMapping("/post/delete/{inquiriesId}")
	public ResponseEntity<?> inquiriesDelete(@PathVariable int inquiriesId,
											 @RequestParam int memberId) {
		
		Map<String, Object> map = new HashMap<>();
		map.put("inquiriesId", inquiriesId);
		map.put("memberId", memberId);
		
		int result = service.inquiriesDelete(map);
		
		if (result > 0) {
			return ResponseEntity.ok("건의글 삭제 성공");
		}else {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
								 .body("건의글 삭제 처리 중 오류 발생");
		}
		
	}
	
	//건의글 상태 처리 - 관리자 권한
	@Operation(summary = "(관리자) 건의글 상태 처리", 
				description = "memberId : 로그인된 사용자 번호   \n\n"
							+ "inquiriesId : 상태 변경할 건의글 번호   \n\n"
							+ "status : 변경할 상태값   \n\n"
							+ "		 -> SUBMITTED(기본값-접수 완료), PROCESSING(진행 중), COMPLETED(답변 완료)")
	@PutMapping("/changeStatus")
	public ResponseEntity<?> inquiriesStatus(@RequestParam int memberId,
										     @RequestParam int inquiriesId,
										     @RequestParam String status
	) {
		if(memberId != 1) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
								 .body("처리 권한이 없습니다.");
		}
		
		try {
			int result = service.inquiriesStatus(inquiriesId, status);
			
			if (result > 0) {
			
				if(status.equals("PROCESSING")) {
					return ResponseEntity.ok("건의글을 '진행 중' 상태로 변경하였습니다.");
				
				}else if(status.equals("COMPLETED")) {
					return ResponseEntity.ok("건의글을 '답변 완료' 상태로 변경하였습니다.");
				}else {
					return ResponseEntity.status(HttpStatus.BAD_REQUEST)
										 .body("존재하지 않는 상태값입니다.");
				}
				
			}else {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
									 .body("잘못된 요청입니다.");
			}
		}catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					 .body("처리 중 오류 발생");
		}
	}
	
	//건의글 답변 - 관리자 권한
	@Operation(summary = "(관리자) 건의글 답변", 
				description = "inquiriesId : 답변(수정)할 건의글 번호   \n\n"
							+ "memberId : 로그인된 사용자 아이디   \n\n"
							+ "adminReply : 답변(등록, 수정, 삭제) 내용  \n\n"
							+ "		-> 기존 답변이 없었으면 신규 등록으로 처리    \n\n"
							+ "		-> 기존 답변이 있었으면 수정으로 처리    \n\n"
							+ "		-> adminReply에 빈칸(null) 입력하면 삭제로 처리")
	@PutMapping("/adminReply/{inquiriesId}")
	public ResponseEntity<?> inquiriesAdmintReply(@PathVariable int inquiriesId,
												  @RequestParam int memberId,
												  @RequestParam(required = false) String adminReply
	) {
		if(memberId != 1) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
								 .body("관리자만 답변을 작성할 수 있습니다.");
		}
		
		InquiriesVO inquiry = service.inquiriesDetail(inquiriesId);
		
		// 게시글 자체가 없는 경우 방어 로직
	    if(inquiry == null) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                             .body("존재하지 않는 게시글입니다.");
	    }
	    
	    String oldReply = inquiry.getAdminReply();
		
		Map<String, Object> map = new HashMap<>();
		map.put("inquiriesId", inquiriesId);
		map.put("adminReply", adminReply);
		

		int result = service.inquiriesAdmintReply(map);
		
		if(result > 0) {
			if (adminReply == null || adminReply.equals("")) {
	            return ResponseEntity.ok("답변 삭제 성공");
	            
	        } else if (oldReply == null || oldReply.equals("")) {
	            // 기존 답변이 없었다면 등록
	            return ResponseEntity.ok("답변 등록 성공");
	            
	        } else {
	            // 기존 답변이 있었다면 수정
	            return ResponseEntity.ok("답변 수정 성공");
	        }
			
	    } else {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                             .body("답변 처리 중 오류 발생");
	    }
	}
	

}
