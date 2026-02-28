package com.kh.spring.community.model.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.session.RowBounds;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import com.kh.spring.common.model.vo.PageInfo;
import com.kh.spring.community.model.vo.CommunityPostVO;
import com.kh.spring.community.model.vo.CommunityReplyVO;
import com.kh.spring.community.model.vo.PostFilesVO;

@Repository
public class CommunityDao {
	
	//게시글 총 개수
	public int listCount(SqlSessionTemplate sqlSession) {
		return sqlSession.selectOne("communityMapper.listCount");
	}
	
	//검색된 게시글 개수
	public int searchListCount(SqlSessionTemplate sqlSession, HashMap<String, String> map) {
		return sqlSession.selectOne("communityMapper.searchListCount", map);
	}

	//필터링된 게시글 개수
	public int filterListCount(SqlSessionTemplate sqlSession, HashMap<String, String> map) {
		return sqlSession.selectOne("communityMapper.filterListCount", map);
	}

	//게시글 검색 조회
	public ArrayList<CommunityPostVO> searchList(SqlSessionTemplate sqlSession, HashMap<String, String> map,
			PageInfo pi) {
		
		int limit = pi.getBoardLimit();
		int offset = (pi.getCurrentPage() - 1) * limit;
		
		RowBounds rowBounds = new RowBounds(offset, limit);
		
		return (ArrayList)sqlSession.selectList("communityMapper.searchList", map, rowBounds);
	}
	
	//게시글 필터링 조회
	public ArrayList<CommunityPostVO> filterList(SqlSessionTemplate sqlSession, HashMap<String, String> map,
			PageInfo pi) {
		
		int limit = pi.getBoardLimit();
		int offset = (pi.getCurrentPage() - 1) * limit;
		
		RowBounds rowBounds = new RowBounds(offset, limit);
		
		return (ArrayList)sqlSession.selectList("communityMapper.filterList", map, rowBounds);
	}
	
	//게시글 목록 조회
	public ArrayList<CommunityPostVO> communityList(SqlSessionTemplate sqlSession, PageInfo pi) {
		
		int limit = pi.getBoardLimit();
		int offset = (pi.getCurrentPage() - 1) * limit;
		
		RowBounds rowBounds = new RowBounds(offset, limit);
		
		return (ArrayList)sqlSession.selectList("communityMapper.communityList", null, rowBounds);
	}
	
	//게시글 등록
	public int communityInsert(SqlSessionTemplate sqlSession, CommunityPostVO cp) {
		return sqlSession.insert("communityMapper.communityInsert",cp);
	}

	//첨부파일 추가
	public int insertPostFile(SqlSessionTemplate sqlSession, ArrayList<PostFilesVO> pfList) {
		return sqlSession.insert("communityMapper.insertPostFile", pfList);
	}
	
	//게시글 수정 - 삭제 대상 파일 조회
    public ArrayList<PostFilesVO> selectFilesByIds(SqlSessionTemplate sqlSession,
            int postId,
            ArrayList<Integer> delFileIds) {
	
    	HashMap<String, Object> param = new HashMap<>();
        param.put("postId", postId);
        param.put("delFileIds", delFileIds);

        return (ArrayList) sqlSession.selectList(
                "communityMapper.selectFilesByIds", param);
    }
	
	//게시글 수정 - 텍스트 정보 수정
	public int updatePost(SqlSessionTemplate sqlSession, CommunityPostVO cp) {
        return sqlSession.update("communityMapper.updatePost", cp);
    }


	//게시글 수정 - 실제 파일 데이터 삭제
    public int deleteFilesByIds(SqlSessionTemplate sqlSession,
                               int postId,
                               ArrayList<Integer> delFileIds) {

        HashMap<String, Object> param = new HashMap<>();
        param.put("postId", postId);
        param.put("delFileIds", delFileIds);

        return sqlSession.delete("communityMapper.deleteFilesByIds", param);
    }

	
	//해당 게시글 파일 개수
	public int countFilesByPostId(SqlSessionTemplate sqlSession, int postId) {
        return sqlSession.selectOne("communityMapper.countFilesByPostId", postId);
    }

	//게시글 수정 - 파일 첨부 여부 갱신
    public int updateHasFiles(SqlSessionTemplate sqlSession,
                              int postId,
                              int hasFiles) {

        HashMap<String, Object> param = new HashMap<>();
        param.put("postId", postId);
        param.put("hasFiles", hasFiles);

        return sqlSession.update("communityMapper.updateHasFiles", param);
    }

    //게시글 상세보기
    public CommunityPostVO communityDetail(SqlSessionTemplate sqlSession, int postId) {
    	return sqlSession.selectOne("communityMapper.communityDetail", postId);
    }
	
	//게시글 첨부파일 목록 조회
	public ArrayList<PostFilesVO> selectFilesByPostIds(SqlSessionTemplate sqlSession, int postId) {
		return (ArrayList)sqlSession.selectList("communityMapper.selectFilesByPostIds", postId);
	}

	//게시글 삭제
	public int communityDelete(SqlSessionTemplate sqlSession, int postId) {
		return sqlSession.delete("communityMapper.communityDelete", postId);
	}

	//게시글 조회수 증가
	public int increaseViewCount(SqlSessionTemplate sqlSession, int postId) {
		return sqlSession.update("communityMapper.increaseViewCount", postId);
	}

	//부모 댓글 정보 조회
	public CommunityReplyVO selectParentReply(SqlSessionTemplate sqlSession, int parentReplyId) {
		return sqlSession.selectOne("communityMapper.selectParentReply", parentReplyId);
	}
	
	//댓글 목록 조회
	public ArrayList<CommunityReplyVO> replyList(SqlSessionTemplate sqlSession, int postId) {
		return (ArrayList)sqlSession.selectList("communityMapper.replyList", postId);
	}

	//댓글 등록
	public int replyInsert(SqlSessionTemplate sqlSession, CommunityReplyVO reply) {
		return sqlSession.insert("communityMapper.replyInsert", reply);
	}
	
	//댓글 수 증감 값 DB 업데이트
	public int updatePostCommentCount(SqlSessionTemplate sqlSession, Map<String, Object> map) {
		return sqlSession.update("communityMapper.updatePostCommentCount", map);
	}

	//댓글 수정
	public int replyUpdate(SqlSessionTemplate sqlSession, CommunityReplyVO reply) {
		return sqlSession.update("communityMapper.replyUpdate", reply);
	}

	//댓글 삭제
	public int replyDelete(SqlSessionTemplate sqlSession, CommunityReplyVO reply) {
		return sqlSession.delete("communityMapper.replyDelete", reply);
	}

	//이미 좋아요를 누른 글인지 확인
	public int checkPostLike(SqlSessionTemplate sqlSession, Map<String, Object> map) {
		return sqlSession.selectOne("communityMapper.checkPostLike", map);
	}
	
	//게시글 좋아요 등록
	public int insertPostLike(SqlSessionTemplate sqlSession, Map<String, Object> map) {
		return sqlSession.insert("communityMapper.insertPostLike", map);
	}

	//게시글 좋아요 상태 변경
	public int changePostLike(SqlSessionTemplate sqlSession, Map<String, Object> map) {
		return sqlSession.update("communityMapper.changePostLike", map);
	}
	
	//게시글 좋아요 상태 변경 값 가져오기
	public String getPostLikeStatus(SqlSessionTemplate sqlSession, Map<String, Object> map) {
		return sqlSession.selectOne("communityMapper.getPostLikeStatus", map);
	}

	//게시글 좋아요 증감 값 DB 업데이트
	public int updatePostLikeCount(SqlSessionTemplate sqlSession, Map<String, Object> map) {
		return sqlSession.update("communityMapper.updatePostLikeCount", map);
	}

	//이미 좋아요를 누른 댓글인지 확인
	public int checkReplyLike(SqlSessionTemplate sqlSession, Map<String, Object> map) {
		return sqlSession.selectOne("communityMapper.checkReplyLike", map);
	}

	//댓글 좋아요 등록
	public int insertReplyLike(SqlSessionTemplate sqlSession, Map<String, Object> map) {
		return sqlSession.insert("communityMapper.insertReplyLike", map);
	}

	//댓글 좋아요 상태 변경
	public int changeReplyLike(SqlSessionTemplate sqlSession, Map<String, Object> map) {
		return sqlSession.update("communityMapper.changeReplyLike", map);
	}

	//댓글 좋아요 상태 변경 값 가져오기
	public String getReplyLikeStatus(SqlSessionTemplate sqlSession, Map<String, Object> map) {
		return sqlSession.selectOne("communityMapper.getReplyLikeStatus", map);
	}

	//댓글 좋아요 증감 값 DB 업데이트
	public int updateReplyLikeCount(SqlSessionTemplate sqlSession, Map<String, Object> map) {
		return sqlSession.update("communityMapper.updateReplyLikeCount", map);
	}






	

	
	
	






}
