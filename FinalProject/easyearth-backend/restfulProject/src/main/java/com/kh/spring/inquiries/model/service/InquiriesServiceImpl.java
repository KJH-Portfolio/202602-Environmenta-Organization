package com.kh.spring.inquiries.model.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.spring.common.model.vo.PageInfo;
import com.kh.spring.inquiries.model.dao.InquiriesDao;
import com.kh.spring.inquiries.model.vo.InquiriesVO;

@Service
public class InquiriesServiceImpl implements InquiriesService {
	
	@Autowired
	private InquiriesDao dao;
	
	@Autowired
	private SqlSessionTemplate sqlSession;
	
	//건의글 총 개수
	@Override
	public int listCount() {
		return dao.listCount(sqlSession);
	}

	//검색된 건의글 개수
	@Override
	public int searchListCount(HashMap<String, Object> map) {
		return dao.searchListCount(sqlSession, map);
	}

	//필터링된 건의글 개수
	@Override
	public int filterListCount(HashMap<String, Object> map) {
		return dao.filterListCount(sqlSession, map);
	}

	//건의글 목록 조회
	@Override
	public ArrayList<InquiriesVO> inquiriesList(PageInfo pi) {
		return dao.inquiriesList(sqlSession, pi);
	}

	//건의글 검색 조회
	@Override
	public ArrayList<InquiriesVO> searchList(HashMap<String, Object> map, PageInfo pi) {
		return dao.searchList(sqlSession, map, pi);
	}

	//건의글 필터링 조회
	@Override
	public ArrayList<InquiriesVO> filterList(HashMap<String, Object> map, PageInfo pi) {
		return dao.filterList(sqlSession, map, pi);
	}
	
	//건의글 조회수 증가
	@Override
	public int increaseCount(int inquiriesId) {
		return dao.increaseCount(sqlSession, inquiriesId);
	}

	//건의글 상세보기
	@Override
	public InquiriesVO inquiriesDetail(int inquiriesId) {
		return dao.inquiriesDetail(sqlSession, inquiriesId);
	}

	//건의글 등록
	@Override
	@Transactional
	public int inquiriesInsert(Map<String, Object> map) {
		return dao.inquiriesInsert(sqlSession, map);
	}

	//건의글 수정
	@Override
	public int inquiriesUpdate(Map<String, Object> map) {
		return dao.inquiriesUpdate(sqlSession, map);
	}

	//건의글 삭제
	@Override
	public int inquiriesDelete(Map<String, Object> map) {
		return dao.inquiriesDelete(sqlSession, map);
	}
  
  //건의글 상태 처리 - 관리자 권한
	@Override
	public int inquiriesStatus(int inquiriesId, String status) {
		return dao.inquiriesStatus(sqlSession, inquiriesId, status);
	}

	//건의글 답변 - 관리자 권한
	@Override
	public int inquiriesAdmintReply(Map<String, Object> map) {
		
		return dao.inquiriesAdmintReply(sqlSession, map);
	}



	
	
}
