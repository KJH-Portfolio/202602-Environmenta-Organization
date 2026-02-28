package com.kh.spring.community.model.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.spring.common.model.vo.PageInfo;
import com.kh.spring.community.model.dao.CommunityDao;
import com.kh.spring.community.model.vo.CommunityPostVO;
import com.kh.spring.community.model.vo.CommunityReplyVO;
import com.kh.spring.community.model.vo.PostFilesVO;
import com.kh.spring.util.FileUtil;

@Service
public class CommunityServiceImpl implements CommunityService {
	
	@Autowired
	private CommunityDao dao;
	
	@Autowired
	private SqlSessionTemplate sqlSession;
	
	@Autowired
	private FileUtil fileUtil;
	
	//게시글 총 개수
	@Override
	public int listCount() {
		return dao.listCount(sqlSession);
	}
	
	//검색된 게시글 개수
	@Override
	public int searchListCount(HashMap<String, String> map) {
		return dao.searchListCount(sqlSession, map);
	}
	
	//필터링된 게시글 개수
	@Override
	public int filterListCount(HashMap<String, String> map) {
		return dao.filterListCount(sqlSession, map);
	}

	//게시글 목록 조회
	@Override
	public ArrayList<CommunityPostVO> communityList(PageInfo pi) {
		return dao.communityList(sqlSession, pi);
	}

	//게시글 검색 조회
	@Override
	public ArrayList<CommunityPostVO> searchList(HashMap<String, String> map, PageInfo pi) {
		return dao.searchList(sqlSession, map, pi);
	}

	//게시글 필터링 조회
	@Override
	public ArrayList<CommunityPostVO> filterList(HashMap<String, String> map, PageInfo pi) {
		return dao.filterList(sqlSession, map, pi);
	}

	//게시글 상세보기
	@Override
	public CommunityPostVO communityDetail(int postId) {
		return dao.communityDetail(sqlSession, postId);
	}

	//게시글 등록
	@Transactional
	@Override
	public int communityInsert(CommunityPostVO cp, ArrayList<PostFilesVO> pfList) {
		
		// 게시글 정보 등록
        int result = dao.communityInsert(sqlSession, cp);
        
        // 게시글 등록 성공 시 파일 처리
        if (result > 0) {
            
            // 파일이 없는 게시글일 수도 있으니 체크
            if (pfList != null && !pfList.isEmpty()) {
                
                // 파일마다 생성된 게시글 번호(postId) 세팅
                for (PostFilesVO pf : pfList) {
                    pf.setPostId(cp.getPostId());
                    pf.setMemberId(cp.getMemberId());
                    
                }
                
                // 파일 리스트 일괄 등록
                int result2 = dao.insertPostFile(sqlSession, pfList);
                
                //둘 중 하나라도 0이면 실패 처리
                return result * result2;
            }
            
            // 파일이 없는 경우 게시글 성공 결과만 반환
            return result;
            
        } else {
            // 게시글 등록 자체가 실패한 경우
            return 0;
        }
    }
	
	//게시글 수정 - 삭제 대상 파일 조회
	@Override
	public ArrayList<PostFilesVO> selectFilesByIds(int postId, ArrayList<Integer> delFileIds) {
		return dao.selectFilesByIds(sqlSession, postId, delFileIds);
	}

	//게시글 수정
	@Transactional
	@Override
	public int communityUpdate(CommunityPostVO cp, ArrayList<PostFilesVO> newPfList, ArrayList<Integer> delFileIds) {
		
		// 게시글 텍스트 수정
        int result = dao.updatePost(sqlSession, cp);
        if (result <= 0) return 0;

        // 파일 삭제(DB)
        if (delFileIds != null && !delFileIds.isEmpty()) {
            dao.deleteFilesByIds(sqlSession, cp.getPostId(), delFileIds);
        }
		
        // 파일 추가(DB)
		if (newPfList != null && !newPfList.isEmpty()) {
			int newResult = dao.insertPostFile(sqlSession, newPfList);
		}
		
		// hasFiles 최종 결정(DB 기준)
        int fileCount = dao.countFilesByPostId(sqlSession, cp.getPostId());
        int hasFiles = (fileCount > 0) ? 1 : 0;
        dao.updateHasFiles(sqlSession, cp.getPostId(), hasFiles);

        return result;
    }

	//게시글 첨부파일 목록 조회
	@Override
	public ArrayList<PostFilesVO> selectFilesByPostIds(int postId) {
		return dao.selectFilesByPostIds(sqlSession, postId);
	}

	//게시글 삭제
	@Override
	public int communityDelete(int postId) {
		return dao.communityDelete(sqlSession, postId);
	}

	//게시글 조회수 증가
	@Override
	public int increaseViewCount(int postId) {
		return dao.increaseViewCount(sqlSession, postId);
	}
	
	//댓글 목록 조회
	@Override
	public ArrayList<CommunityReplyVO> replyList(int postId) {
		return dao.replyList(sqlSession, postId);
	}

	//댓글 등록
	@Transactional
	@Override
	public int replyInsert(CommunityReplyVO reply) {
		
		//새로운 일반 댓글인 경우
		if (reply.getParentReplyId() == 0) {
			reply.setDepth(0);
		}else {
			CommunityReplyVO parent = dao.selectParentReply(sqlSession, reply.getParentReplyId());
			
			if(parent == null) return 0;  //부모 아이디 없으면 에러
			
			reply.setGroupId(parent.getGroupId());  //부모 댓글과 같은 그룹
			reply.setDepth(parent.getDepth() + 1);  //부모 댓글보다 한 단계 깊은 계층
		}
		
		int result = dao.replyInsert(sqlSession, reply);
		
		//댓글 수 증감
		if(result > 0) {
			Map<String, Object> map = new HashMap<>();
			map.put("postId", reply.getPostId());
			map.put("count", 1);
			dao.updatePostCommentCount(sqlSession, map);
		}
		
		return result;
	}

	//댓글 수정
	@Override
	public int replyUpdate(CommunityReplyVO reply) {
		return dao.replyUpdate(sqlSession, reply);
	}

	//댓글 삭제
	@Transactional
	@Override
	public int replyDelete(CommunityReplyVO reply) {
		int result = dao.replyDelete(sqlSession, reply);
		
		if(result > 0) {
			Map<String, Object> map = new HashMap<>();
			map.put("postId", reply.getPostId());
			map.put("count", -1);
			dao.updatePostCommentCount(sqlSession, map);
		}
		return result;
	}

	//게시글 좋아요
	@Override
	public String communityLikes(Map<String, Object> map) {
		
		//이미 좋아요를 누른 게시글인지 확인
		int check = dao.checkPostLike(sqlSession, map);
		String resultMsg = "";
		int count = 0;

		if (check == 0) {
			dao.insertPostLike(sqlSession, map);
			count = 1;
			resultMsg = "좋아요 등록";
			
		}else {
			dao.changePostLike(sqlSession, map);
			
			String currentStatus = dao.getPostLikeStatus(sqlSession, map);
			
			if(currentStatus.equals("Y")) {
				count = 1;
				resultMsg = "좋아요 등록";
			}else {
				count = -1;
				resultMsg = "좋아요 취소";
			}
		}
		
		map.put("count", count);
		dao.updatePostLikeCount(sqlSession, map);
		
		return resultMsg;
	}
	
	//게시글 좋아요 상태 조회
	@Override
	public String getPostLikeStatus(Map<String, Object> map) {
		return dao.getPostLikeStatus(sqlSession, map);
	}

	//댓글 좋아요
	@Override
	public String replyLikes(Map<String, Object> map) {
		
		//이미 좋아요를 누른 댓글인지 확인
		int check = dao.checkReplyLike(sqlSession, map);
		String resultMsg = "";
		int count = 0;
		
		if (check == 0) {
			dao.insertReplyLike(sqlSession, map);
			count = 1;
			resultMsg = "댓글 좋아요 등록";
			
		}else {
			dao.changeReplyLike(sqlSession, map);
			
			String currentStatus = dao.getReplyLikeStatus(sqlSession, map);
			
			if(currentStatus.equals("Y")) {
				count = 1;
				resultMsg = "댓글 좋아요 등록";
			}else {
				count = -1;
				resultMsg = "댓글 좋아요 취소";
			}
		}
		map.put("count", count);
		dao.updateReplyLikeCount(sqlSession, map);
		
		return resultMsg;
	}

	//댓글 좋아요 상태 조회
	@Override
	public String getReplyLikeStatus(Map<String, Object> map) {
		return dao.getReplyLikeStatus(sqlSession, map);
	}



	
	






	
}
