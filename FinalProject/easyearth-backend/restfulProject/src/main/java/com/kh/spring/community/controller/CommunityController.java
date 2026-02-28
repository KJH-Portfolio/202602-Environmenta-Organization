package com.kh.spring.community.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.kh.spring.common.model.vo.PageInfo;
import com.kh.spring.common.template.Pagination;
import com.kh.spring.community.model.service.CommunityService;
import com.kh.spring.community.model.vo.CommunityListDTO;
import com.kh.spring.community.model.vo.CommunityPostVO;
import com.kh.spring.community.model.vo.CommunityReplyVO;
import com.kh.spring.community.model.vo.PostFilesVO;
import com.kh.spring.util.FileUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/community")
@Slf4j
@Tag(name = "커뮤니티", description = "커뮤니티")
public class CommunityController {
	
	@Autowired
	private CommunityService service;
	
	@Autowired
	private FileUtil fileUtil;
	
	//게시글 목록 || 검색 목록 || 필터링 목록
    @Operation(summary = "게시글 목록 조회", 
    		description =  "condition : title(제목) / writer(작성자) / content(내용)   \n\n"
						+ "keyword : condition에 대한 검색어   \n\n"
						+ "status(등록 상태) : Y(정상-기본값) / N(삭제) / B(블라인드)   \n\n")
	@GetMapping("/post/list")
	public ResponseEntity<CommunityListDTO> communityList(
			@RequestParam(value="page", defaultValue = "1") int currentPage,
	        @RequestParam(defaultValue = "10") int size,
	        @RequestParam(required = false) String condition,
	        @RequestParam(required = false) String keyword,
	        @RequestParam(required = false) String category) {
		
		int listCount = 0;
		int boardLimit = 10;
		int pageLimit = size;
		
		HashMap<String, String> map = new HashMap<>();
		
		if (keyword != null && !keyword.isEmpty()) {
			map.put("keyword", keyword);
			map.put("condition", condition);
			listCount = service.searchListCount(map); // 검색된 개수
			
		}else if (category != null && !category.isEmpty()) {
			map.put("category", category);
			listCount = service.filterListCount(map); // 필터링된 개수
			
		}else {
			listCount = service.listCount();  //전체 개수
		}
		
		PageInfo pi = Pagination.getPageInfo(listCount, currentPage, boardLimit, pageLimit);
		
		ArrayList<CommunityPostVO> list;
		
		
		if (keyword != null && !keyword.isEmpty()) {
			list = service.searchList(map, pi);
		}else if (category != null && !category.isEmpty()) {
			list= service.filterList(map, pi);
		}else {
			list = service.communityList(pi);
		}
		
		return ResponseEntity.ok(CommunityListDTO.of(list, pi));		
	}
    
    //게시글 상세보기
    @Operation(summary = "게시글 상세보기", description = "postId : 조회할 게시글 번호")
    @GetMapping("/post/detail/{postId}")
    public ResponseEntity<?> communityDetail(@PathVariable int postId) {
    	
    	try {
    		
    		CommunityPostVO cp = service.communityDetail(postId);
            
            if(cp == null) {
                return ResponseEntity.status(404)
                    .body("게시글을 찾을 수 없습니다.");
            }
            
            // 블라인드 처리된 게시글 체크
            if(cp.getStatus().equals("B")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("누적 신고로 인해 블라인드 처리된 게시글입니다.");
            }
    		
    		int result = service.increaseViewCount(postId);
        	
        	if(result > 0) {
        		
        		//게시글 첨부파일 정보
        		ArrayList<PostFilesVO> fileList = service.selectFilesByPostIds(postId);
        		
        		//텍스트와 첨부파일 데이터 하나로 묶기
        		Map<String, Object> map = new HashMap<>();
        		map.put("cp", cp);
        		map.put("fileList", fileList);
        		
        		return ResponseEntity.ok(map);
        		
        	}else {
        		return ResponseEntity.status(404)
        							 .body("게시글을 찾을 수 없습니다.");
        	}
    	}catch (Exception e) {
    		e.printStackTrace();
    		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    							 .body("조회 중 오류 발생");
    	}
    }
    
    //게시글 등록
    @Operation(summary = "게시글 등록", 
    			description = "memberId : 로그인된 사용자 아이디 \n\n"
							+ "title : 게시글 제목 \n\n"
							+ "content : 게시글 내용 \n\n"
							+ "category : 나눔 / 자유 / 인증 / 정보 / 기타   \n\n"
							+ "uploadFile : 업로드 할 첨부파일")
    @PostMapping(value = "/post/insert", consumes = MediaType.MULTIPART_FORM_DATA_VALUE) // 1. 미디어 타입 명시
    public ResponseEntity<?> communityInsert(
 		    @RequestParam("memberId") int memberId,
 		    @RequestParam("title") String title,
 		    @RequestParam("content") String content,
 		    @RequestParam("category") String category,
            @RequestPart(value = "uploadFile", required = false) ArrayList<MultipartFile> uploadFiles // 2. @RequestPart 명시
     
    ) {
    	try {
			System.out.println("insert");
    		//카테고리 정보 체크
    		if(category == null || category.isEmpty()) {
    			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
    								 .body("카테고리를 선택해주세요.");
    		}
    		
    		// 1. 게시글 정보 담기
            CommunityPostVO cp = new CommunityPostVO();
            cp.setMemberId(memberId);
	        cp.setTitle(title);
	        cp.setContent(content);
	        cp.setCategory(category);
	        cp.setHasFiles(uploadFiles != null && !uploadFiles.isEmpty() ? 1 : 0);
            
            // 2. 파일들 가공
            ArrayList<PostFilesVO> pfList = new ArrayList<>();
            if (uploadFiles != null && !uploadFiles.isEmpty()) {

            	for (MultipartFile file : uploadFiles) {

            		if (!file.isEmpty()) {
                    	String originName = file.getOriginalFilename();
                    	String changeName = fileUtil.saveFile(file, "community"); // 서버에 저장
                        
                        PostFilesVO pf = new PostFilesVO();
                        pf.setOriginName(originName);
                        pf.setChangeName(changeName);
                        pf.setUrl(changeName);
                        pf.setType(originName.substring(originName.lastIndexOf(".")));
                        pf.setFileSize((int)Math.round((double) file.getSize() / 1024));  //KB로 반올림 후 저장
                        
                        pfList.add(pf);
                    }
                }
            } 
            
            int result = service.communityInsert(cp, pfList); 
            
            if (result > 0) {
                return ResponseEntity.ok("게시글 등록 성공");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("게시글 등록 실패");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 에러 발생");
        }
	}
	
    //게시글 수정
    @Operation(summary = "게시글 수정", 
    			description = "postId : 수정할 게시글 번호 \n\n"
    						+ "memberId : 로그인된 사용자 아이디 \n\n"
							+ "title : 게시글 제목  \n\n"
							+ "content : 게시글 내용  \n\n"
							+ "category : 나눔 / 자유 / 인증 / 정보 / 기타   \n\n"
							+ "uploadFile : 업로드에 추가할 첨부파일   \n\n"
							+ "delFileIds : 기존 업로드에서 삭제할 첨부파일 번호")
	@PutMapping(value = "/post/update/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> communityUpdate(
	    @PathVariable("postId") int postId,
	    @RequestParam("memberId") int memberId,
	    @RequestParam("title") String title,
	    @RequestParam("content") String content,
	    @RequestParam("category") String category,
        @RequestPart(value = "uploadFile", required = false) ArrayList<MultipartFile> uploadFiles, 
        @RequestParam(value = "delFileIds", required = false) ArrayList<Integer> delFileIds
        
	) {
    	
    	try {
    	
	    	CommunityPostVO cp = new CommunityPostVO();
	    	cp.setPostId(postId);
	    	cp.setMemberId(memberId);
	        cp.setTitle(title);
	        cp.setContent(content);
	        cp.setCategory(category);
	        
	        // 2) 삭제할 파일의 changeName 확보(물리삭제용) - 선택
	        ArrayList<PostFilesVO> delFiles = null;
	        if (delFileIds != null && !delFileIds.isEmpty()) {
	        	delFiles = service.selectFilesByIds(postId, delFileIds);
	        }
	        
	        //새로 추가할 파일 리스트 만들기
	        ArrayList<PostFilesVO> newPfList = new ArrayList<>();
	        
	        if (uploadFiles != null) {
	        	for (MultipartFile file : uploadFiles) {
	
	        		String originName = file.getOriginalFilename();
	        		
	        		if (originName != null && !originName.equals("")) {
	        			
	        			String changeName = fileUtil.saveFile(file, "community");
	        			
	        			PostFilesVO pf = new PostFilesVO();
	        			pf.setPostId(postId);
	        			pf.setMemberId(memberId);
	        			pf.setOriginName(originName);
	        			pf.setChangeName(changeName);
	        			pf.setUrl(changeName);
	        			pf.setType(originName.substring(originName.lastIndexOf(".")));
	        			pf.setFilesId((int)Math.round((double)file.getSize() / 1024));
	        			
	        			newPfList.add(pf);
	        			
	        		}
	        	}
	        }
	        
	        //DB 처리
	        int result = service.communityUpdate(cp, newPfList, delFileIds);
	        
	        //DB 성공 후 실제 파일 삭제
	        if (result > 0 && delFiles != null) {
	        	for (PostFilesVO pf : delFiles) {
	        		try {
	        			fileUtil.deleteFile(pf.getChangeName(), "community");
	        			
	        		}catch (Exception e) {
	        			e.printStackTrace();
	        		}
	        	}
	        }
	        
	        return (result > 0)
	                ? ResponseEntity.ok("게시글 수정 성공")
	                : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("게시글 수정 실패");

	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 에러 발생");
	    }
    }
    
    //게시글 삭제 list 사용으로 수정
    @Operation(summary = "게시글 삭제", description = "postId : 삭제할 게시글 번호")
	@DeleteMapping("/post/delete/{postId}")
	public ResponseEntity<?> communityDelete(@PathVariable int postId) {
		
		//postId로 게시글 조회
		CommunityPostVO cp = service.communityDetail(postId);
		
		if(cp == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
								 .body("게시글을 찾을 수 없습니다.");
		}
		
		// 게시글 첨부파일 목록 조회 (물리 삭제용)
		ArrayList<PostFilesVO> fileList = service.selectFilesByPostIds(postId); 
		
		//게시글 삭제
		int result = service.communityDelete(postId);
		
		if(result > 0) {
		
			if (fileList != null && !fileList.isEmpty()) {
				for (PostFilesVO pf : fileList) {
					if (pf.getChangeName() != null && !pf.getChangeName().equals("")) {
						
						boolean flag = fileUtil.deleteFile(pf.getChangeName(), "community");
						
						if (!flag) {
							log.warn("정보 삭제는 되었지만 파일 삭제 오류 발생");
						}
					}
				}
			}
			return ResponseEntity.ok("게시글이 삭제되었습니다.");
		}
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body("게시글 삭제 처리 중 오류가 발생했습니다.");
    }
    
    //댓글 목록 조회
    @Operation(summary = "댓글 목록 조회", description = "postId : 댓글 조회할 게시글 번호")
    @GetMapping("/reply/list/{postId}")
    public ResponseEntity<?> replyList(@PathVariable int postId) {
    	
    	ArrayList<CommunityReplyVO> rList = service.replyList(postId);
    	
    	return ResponseEntity.ok(rList);
    }
    
    //댓글 등록
    @Operation(summary = "댓글 등록", 
    			description = "postId : 댓글 등록할 게시글 번호   \n\n"
    						+ "memberId : 로그인된 사용자 아이디   \n\n"
    						+ "content : 댓글 내용   \n\n"
    						+ "parentReplyId : 부모 댓글 번호   \n\n"
    						+ "		-> 대댓글이 아닌 일반 댓글 등록 : 0 (기본값)  \n\n"
    						+ "		-> 기존 댓글에 대댓글 등록하는 경우 : 대댓글을 등록하고 싶은 기존 댓글 번호")
	@PostMapping("/reply/insert/{postId}")
    public ResponseEntity<?> replyInsert(@PathVariable int postId,
    							 		 @RequestParam("memberId") int memberId,
    							 		 @RequestParam("content") String content,
    							 		 @RequestParam(value = "parentReplyId", defaultValue = "0") int parentReplyId
    							
    ) {
    	
    	CommunityReplyVO reply = new CommunityReplyVO();
    	reply.setPostId(postId);
    	reply.setMemberId(memberId);
    	reply.setContent(content);
    	reply.setParentReplyId(parentReplyId);
    	
    	int result = service.replyInsert(reply);
    	
    	if (result > 0) {
    		return ResponseEntity.ok("댓글 등록 성공");
    	}else {
    		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    							 .body("댓글 등록 실패");
    	}
    }

    //댓글 수정
    @Operation(summary = "댓글 수정", 
    			description = "postId : 댓글 수정할 게시글 번호   \n\n"
							+ "reply : 수정할 댓글 번호   \n\n"
							+ "memberId : 로그인된 사용자 아이디   \n\n"
    						+ "content : 수정할 댓글 내용   \n\n"
    						+ "		-> 수정할 댓글 작성자와 로그인된 사용자가 동일해야 함")
	@PutMapping("/reply/update/{postId}")
    public ResponseEntity<?> replyUpdate(@PathVariable int postId,
    									 @RequestParam int replyId,
    									 @RequestParam int memberId,
    									 @RequestParam String content
    ) {
    	CommunityReplyVO reply = new CommunityReplyVO();
    	reply.setPostId(postId);
    	reply.setReplyId(replyId);
    	reply.setMemberId(memberId);
    	reply.setContent(content);
    	
    	int result = service.replyUpdate(reply);
    	
    	if (result > 0) {
    		return ResponseEntity.ok("댓글 수정 성공");
    	}else {
    		return ResponseEntity.status(HttpStatus.FORBIDDEN)
    							 .body("댓글 작성자가 아닙니다.");
    	}
    }
    
    //댓글 삭제
    @Operation(summary = "댓글 삭제", 
    			description = "postId : 댓글 삭제할 게시글 번호  \n\n"
    						+ "replyId : 삭제할 댓글 번호  \n\n"
    						+ "memberId : 로그인된 사용자 아이디  \n\n"
    						+ "		-> 삭제할 댓글 작성자와 로그인된 사용자가 동일해야 함")
	@DeleteMapping("/reply/delete/{postId}")
    public ResponseEntity<?> replyDelete(@PathVariable int postId,
    									 @RequestParam int replyId,
    									 @RequestParam int memberId
    ) {
    	CommunityReplyVO reply = new CommunityReplyVO();
    	reply.setPostId(postId);
    	reply.setReplyId(replyId);
    	reply.setMemberId(memberId);
    	
    	int result = service.replyDelete(reply);
    	
    	if (result > 0) {
    		return ResponseEntity.ok("댓글 삭제 성공");
    	}else {
    		return ResponseEntity.status(HttpStatus.FORBIDDEN)
    							 .body("댓글 작성자가 아닙니다.");
    	}
    }
    
    //게시글 좋아요 기능 (등록 / 취소)
    @Operation(summary = "게시글 좋아요", 
    			description = "postId : 좋아요 추가할 게시글 번호   \n\n"
    						+ "memberId : 로그인된 사용자 아이디")
    @PostMapping("/post/{postId}/likes") 
    public ResponseEntity<?> communityLikes (@PathVariable int postId,
    									  	 @RequestParam int memberId
    ) {
    	Map<String, Object> map = new HashMap<>();
    	map.put("postId", postId);
    	map.put("memberId", memberId);
    	
    	String result = service.communityLikes(map);
    	
    	return ResponseEntity.ok(result);
    	
    }
    
    //게시글 좋아요 상태 조회
    @Operation(summary = "게시글 좋아요 상태 조회", 
			description = "postId : 좋아요 상태 조회할 게시글 번호   \n\n"
						+ "memberId : 로그인된 사용자 아이디")
    @GetMapping("/post/{postId}/likes/status")
    public ResponseEntity<?> getPostLikeStatus(@PathVariable int postId,
                                                @RequestParam int memberId) {
        Map<String, Object> map = new HashMap<>();
        map.put("postId", postId);
        map.put("memberId", memberId);
        
        String status = service.getPostLikeStatus(map);
        return ResponseEntity.ok(status != null ? status : "N");
    }
    
    //댓글 좋아요 기능 (등록 / 취소)
    @Operation(summary = "댓글 좋아요", 
    			description = "postId : 해당 댓글의 게시글 번호   \n\n"
    						+ "replyId : 좋아요 추가할 댓글 번호   \n\n"
    						+ "memberId : 로그인된 사용자 아이디")
    @PostMapping("/reply/{postId}/{replyId}/likes") 
    public ResponseEntity<?> replyLikes (@PathVariable int postId,
    									 @PathVariable int replyId,
									  	 @RequestParam int memberId
    ) {
    	Map<String, Object> map = new HashMap<>();
    	map.put("postId", postId);
    	map.put("replyId", replyId);
    	map.put("memberId", memberId);
    	
    	String result = service.replyLikes(map);
    	
    	return ResponseEntity.ok(result);
    	
    }
    
    //댓글 좋아요 상태 조회
    @Operation(summary = "댓글 좋아요 상태 조회", 
			description = "postId : 좋아요 상태 조회할 게시글 번호   \n\n"
						+ "replyId : 좋아요 상태 조회할 댓글 번호   \n\n"
						+ "memberId : 로그인된 사용자 아이디")
    @GetMapping("/reply/{postId}/{replyId}/likes/status")
    public ResponseEntity<?> getReplyLikeStatus(@PathVariable int postId,
                                                 @PathVariable int replyId,
                                                 @RequestParam int memberId) {
        Map<String, Object> map = new HashMap<>();
        map.put("postId", postId);
        map.put("replyId", replyId);
        map.put("memberId", memberId);
        
        String status = service.getReplyLikeStatus(map);
        return ResponseEntity.ok(status != null ? status : "N");
    }
    
    
}
