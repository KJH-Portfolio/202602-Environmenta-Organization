package com.kh.spring.inquiries.model.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.session.RowBounds;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import com.kh.spring.common.model.vo.PageInfo;
import com.kh.spring.inquiries.model.vo.InquiriesVO;

@Repository
public class InquiriesDao {
	
	//건의글 총 개수
	public int listCount(SqlSessionTemplate sqlSession) {
		return sqlSession.selectOne("inquiriesMapper.listCount");
	}

	//검색된 건의글 개수
	public int searchListCount(SqlSessionTemplate sqlSession, Map<String, Object> map) {
		return sqlSession.selectOne("inquiriesMapper.searchListCount", map);
	}

	//필터링된 건의글 개수
	public int filterListCount(SqlSessionTemplate sqlSession, Map<String, Object> map) {
		return sqlSession.selectOne("inquiriesMapper.filterListCount", map);
	}

	//건의글 목록 조회
	public ArrayList<InquiriesVO> inquiriesList(SqlSessionTemplate sqlSession, PageInfo pi) {
		
		int limit = pi.getBoardLimit();
		int offset = (pi.getCurrentPage() - 1) * limit;
		
		RowBounds rowBounds = new RowBounds(offset, limit);
		
		return (ArrayList)sqlSession.selectList("inquiriesMapper.inquiriesList", null, rowBounds);
	}

	//건의글 검색 조회
	public ArrayList<InquiriesVO> searchList(SqlSessionTemplate sqlSession, Map<String, Object> map, PageInfo pi) {
		
		int limit = pi.getBoardLimit();
		int offset = (pi.getCurrentPage() - 1) * limit;
		
		RowBounds rowBounds = new RowBounds(offset, limit);
		
		return (ArrayList)sqlSession.selectList("inquiriesMapper.searchList", map, rowBounds);
	}

	//건의글 필터링 조회
	public ArrayList<InquiriesVO> filterList(SqlSessionTemplate sqlSession, Map<String, Object> map, PageInfo pi) {
		
		int limit = pi.getBoardLimit();
		int offset = (pi.getCurrentPage() - 1) * limit;
		
		RowBounds rowBounds = new RowBounds(offset, limit);
		
		return (ArrayList)sqlSession.selectList("inquiriesMapper.filterList", map, rowBounds);
	}
	
	//건의글 조회수 증가
	public int increaseCount(SqlSessionTemplate sqlSession, int inquiriesId) {
		return sqlSession.update("inquiriesMapper.increaseCount", inquiriesId);
	}

	//건의글 상세보기
	public InquiriesVO inquiriesDetail(SqlSessionTemplate sqlSession, int inquiriesId) {
		return sqlSession.selectOne("inquiriesMapper.inquiriesDetail", inquiriesId);
	}

	//건의글 등록
	public int inquiriesInsert(SqlSessionTemplate sqlSession, Map<String, Object> map) {
		return sqlSession.insert("inquiriesMapper.inquiriesInsert", map);
	}

	//건이글 수정
	public int inquiriesUpdate(SqlSessionTemplate sqlSession, Map<String, Object> map) {
		return sqlSession.update("inquiriesMapper.inquiriesUpdate", map);
	}

	//게시글 삭제
	public int inquiriesDelete(SqlSessionTemplate sqlSession, Map<String, Object> map) {
		return sqlSession.delete("inquiriesMapper.inquiriesDelete", map);
	}

	//건의글 상태 처리 - 관리자 권한
	public int inquiriesStatus(SqlSessionTemplate sqlSession, int inquiriesId, String status) {
		
		HashMap<String, Object> map = new HashMap<>();
		map.put("inquiriesId", inquiriesId);
		map.put("status", status);
		
		return sqlSession.update("inquiriesMapper.inquiriesStatus", map);
	}

	//건의글 답변 - 관리자 권한
	public int inquiriesAdmintReply(SqlSessionTemplate sqlSession, Map<String, Object> map) {
		return sqlSession.update("inquiriesMapper.inquiriesAdmintReply", map);
	}



}
