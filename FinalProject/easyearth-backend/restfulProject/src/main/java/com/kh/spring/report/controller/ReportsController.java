package com.kh.spring.report.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kh.spring.attendance.controller.AttendanceController;
import com.kh.spring.common.model.vo.PageInfo;
import com.kh.spring.common.template.Pagination;
import com.kh.spring.community.model.service.CommunityServiceImpl;
import com.kh.spring.member.model.vo.MemberVO;
import com.kh.spring.report.model.service.ReportsService;
import com.kh.spring.report.model.vo.ReportsListDTO;
import com.kh.spring.report.model.vo.ReportsVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/reports")
@Slf4j
@Tag(name = "신고", description = "신고")
public class ReportsController {

	@Autowired
	private ReportsService service;
	
	@Operation(summary = "신고 목록 조회", 
			description =  "condition : title(제목) / writer(작성자) / content(내용)   \n\n"
						+ "keyword : condition에 대한 검색어   \n\n"
						+ "type(신고 대상 유형) : POST(커뮤니티 게시글) / REPLY(커뮤니티 댓글) / REVIEW (상점 리뷰)  \n\n"
						+ "reason(신고 사유) : 부적절한 콘텐츠 // 스팸/홍보성 // 욕설/비방 // 기타   \n\n"
						+ "status(처리 상태) : RECEIVED(접수 완료) / RESOLVED(처리 완료) / REJECTED(반려)   \n\n"
						+ "				-> 기본 값 : null - 관리자가 접수 처리하기 전 상태, 신고 요청만 보낸 상태")
	@GetMapping("/list")
	public ResponseEntity<ReportsListDTO> reportsList(
			@RequestParam(value="page", defaultValue = "1") int currentPage,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(required = false) String condition,
			@RequestParam(required = false) String keyword,
			@RequestParam(required = false) String type,
			@RequestParam(required = false) String reason,
			@RequestParam(required = false) String status
	) {
		
		int listCount = 0;
		int boardLimit = 10;
		int pageLimit = size;

		HashMap<String, String> map = new HashMap<>();
		
		if (keyword != null && !keyword.isEmpty()) {
			map.put("condition", condition);
			map.put("keyword", keyword);
			listCount = service.searchReportsCount(map);  //검색된 개수
		}else if ((type != null && !type.isEmpty()) || 
					(reason != null && !reason.isEmpty()) ||
					(status != null && !status.isEmpty())) {
			map.put("type", type);
			map.put("reason", reason);
			map.put("status", status);
			listCount = service.filterReportsCount(map);  //필터링된 개수
		}else {
			listCount = service.reportListsCount();  //전체 개수
		}
		
		PageInfo pi = Pagination.getPageInfo(listCount, currentPage ,boardLimit, pageLimit);
		
		ArrayList<ReportsVO> list;
		
		if (keyword != null && !keyword.isEmpty()) {
			list = service.searchReportsList(map, pi);
		}else if ((type != null && !type.isEmpty()) || 
					(reason != null && !reason.isEmpty()) ||
					(status != null && !status.isEmpty())) {
			list = service.filterReportsList(map, pi);
		}else {
			list = service.reportsList(pi);
		}
		
		return ResponseEntity.ok(ReportsListDTO.of(list, pi));
	}
	
	//신고글 상세보기
	@Operation(summary = "신고글 상세보기", description = "reportsId : 조회할 신고글 번호")
	@GetMapping("/detail/{reportsId}")
	public ResponseEntity<?> reportsDetail(@PathVariable int reportsId) {
		
		try {
			ReportsVO reports = service.reportsDetail(reportsId);
			
			if(reports != null) {
				return ResponseEntity.ok(reports);
			}else {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
									 .body("해당 신고 내역을 찾을 수 없습니다.");
			}
		}catch(Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
								 .body("조회 중 오류 발생");
		}
	}
	
	@Operation(summary = "신고 등록", 
				description = "memberId : 로그인된 사용자 아이디 \n\n"
							+ "targetMemberId : 신고할 사용자 아이디 \n\n"
							+ "type(신고 대상 유형) : POST(커뮤니티 게시글) / REPLY(커뮤니티 댓글) / REVIEW (상점 리뷰)  \n\n"
							+ "postId : 신고할 커뮤니티 게시글 번호   \n\n"
							+ "replyId : 신고할 커뮤니티 댓글 번호   \n\n"
							+ "reviewId : 신고할 상점 리뷰 번호   \n\n"
							+ "reason(신고 유형) : ----기능 수정 중----) \n\n"
							+ "detail : 신고 내용 \n\n")
	@PostMapping("/insert")
	public ResponseEntity<?> reportsInsert (@RequestParam int memberId,
										    @RequestParam int targetMemberId,
										    @RequestParam String type,
										    @RequestParam(value="postId", required=false, defaultValue="0") int postId,
										    @RequestParam(value="replyId", required=false, defaultValue="0") int replyId,
										    @RequestParam(value="reviewId", required=false, defaultValue="0") int reviewId,
										    @RequestParam String reason,
										    @RequestParam String detail
	) {
		
		Map<String, Object> map = new HashMap<>();
		map.put("memberId", memberId);
		map.put("targetMemberId", targetMemberId);
		map.put("type", type);
		map.put("postId", postId);
		map.put("replyId", replyId);
		map.put("reviewId", reviewId);
		map.put("reason", reason);
		map.put("detail", detail);
		
		int result = service.reportsInsert(map);
		
		if(result > 0) {
			return ResponseEntity.ok("신고 등록 성공");
		}else {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					 			 .body("신고 등록 실패");
		}
	}

	@Operation(summary = "신고 수정", 
			description = "reportsId : 수정할 신고글 번호  \n\n"
						+ "memberId : 로그인된 사용자 아이디    \n\n"
						+ "reason(신고 유형) : ----기능 수정 중----  \n\n"
						+ "detail : 신고 내용  \n\n"
						+ "		-> 삭제할 신고글 작성자와 로그인된 사용자가 동일해야 함")
	@PutMapping("/update/{reportsId}")
	public ResponseEntity<?> reportsUpdate(@PathVariable int reportsId,
										   @RequestParam int memberId,
										   @RequestParam(required = false) String reason,
										   @RequestParam(required = false) String detail
	) {
		ReportsVO reports = ReportsVO.builder()
					                .reportsId(reportsId)
					                .memberId(memberId)
					                .reason(reason)
					                .detail(detail)
					                .build();
		
		int result = service.reportsUpdate(reports);
		
		if (result > 0) {
			return ResponseEntity.ok("신고 수정 성공");
		}else {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
								 .body("수정 가능한 상태가 아닙니다.");
		}
	}
	
	@Operation(summary = "신고 삭제", 
				description = "reportsId : 삭제할 신고글 번호  \n\n"
							+ "memberId : 로그인된 사용자 아이디   \n\n"
							+ "		-> 삭제할 신고글 작성자와 로그인된 사용자가 동일해야 함")
	@DeleteMapping("/delete/{reportsId}")
	public ResponseEntity<?> reportsDelete(@PathVariable int reportsId,
										   @RequestParam int memberId
	) {
		
		ReportsVO reports = ReportsVO.builder()
									 .reportsId(reportsId)
									 .memberId(memberId)
									 .build();
		
		int result = service.reportsDelete(reports);
		
		if (result > 0) {
			return ResponseEntity.ok("신고 삭제 성공");
		}else {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
								 .body("삭제 가능한 상태가 아닙니다.");
		}
	}
	
	//신고글 상태 처리 - 관리자 권한
	@Operation(summary = "(관리자) 신고글 상태 처리", 
				description = "memberId : 로그인된 사용자 아이디 -> 관리자 : 1   \n\n"
							+ "reportsId : 상태값 변경할 신고글 번호   \n\n"
							+ "status(처리 상태) : RECEIVED(접수 완료) / RESOLVED(처리 완료) / REJECTED(반려)")
	@PutMapping("/changeStatus")
	public ResponseEntity<?> reportsStatus(@RequestParam int memberId,
										   @RequestParam int reportsId,
										   @RequestParam String status
	) {
		if(memberId != 1) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
								 .body("처리 권한이 없습니다.");
		}
		
		try {
			int result = service.reportsStatus(reportsId, status);
			
			if (result > 0) {
				return ResponseEntity.ok("처리 완료");
			}else {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
									 .body("잘못된 요청입니다.");
			}
		}catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					 .body("처리 중 오류 발생");
		}
	}
	
	//누적 신고 10회 블라인드 처리 
	@Operation(summary = "누적 신고 10회 블라인드 처리", 
				description = "type(신고 대상 유형) : POST(커뮤니티 게시글) / REPLY(커뮤니티 댓글) / REVIEW (상점 리뷰)  \n\n"
							+ "postId : 신고할 커뮤니티 게시글 번호   \n\n"
							+ "replyId : 신고할 커뮤니티 댓글 번호   \n\n"
							+ "reviewId : 신고할 상점 리뷰 번호   \n\n")
	@PutMapping("/blind")
	public ResponseEntity<?> reportsBlind(@RequestParam String type,
									      @RequestParam(value="postId", required=false, defaultValue="0") int postId,
									      @RequestParam(value="replyId", required=false, defaultValue="0") int replyId,
									      @RequestParam(value="reviewId", required=false, defaultValue="0") int reviewId
	) {

		Map<String, Object> map = new HashMap<>();
		map.put("type", type);
		map.put("postId", postId);
		map.put("replyId", replyId);
		map.put("reviewId", reviewId);
		
		try {
			int result = service.reportsBlind(map);
			
			if(result > 0) {
				return ResponseEntity.ok("누적 신고 10회 : 블라인트 처리 완료");
			}else {
				return ResponseEntity.ok("조건 미달 : 누적 횟수 10회 미만입니다.");
			}
		}catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
								 .body("블라인드 자동 처리 중 오류 발생");
		}
	}
	
	@Operation(summary = "신고 중복 여부 확인", description = "신고 중복 여부 확인")
	@GetMapping("/check/{memberId}/{targetMemberId}")
	public ResponseEntity<?> reportsCheck(@PathVariable int memberId,
										  @PathVariable int targetMemberId,
										  @RequestParam(value="postId", required=false, defaultValue="0") int postId,
										  @RequestParam(value="replyId", required=false, defaultValue="0") int replyId,
										  @RequestParam(value="reviewId", required=false, defaultValue="0") int reviewId) {
		int result = service.reportsCheck(memberId, targetMemberId,postId,replyId,reviewId);
		if(result == 0) {
			return ResponseEntity.ok("신고 기록이 없습니다.");
		}
		else {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("신고 기록이 존재합니다.");
		}
	}
}
