package com.kh.spring.report.model.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.spring.common.model.vo.PageInfo;
import com.kh.spring.global.controller.GlobalEcoNewsController;
import com.kh.spring.report.model.dao.ReportsDao;
import com.kh.spring.report.model.vo.ReportsVO;

import lombok.extern.slf4j.Slf4j;

@Service
public class ReportsServiceImpl implements ReportsService{

    private final GlobalEcoNewsController globalEcoNewsController;

	@Autowired
	private ReportsDao dao;
	
	@Autowired
	private SqlSessionTemplate sqlSession;

    ReportsServiceImpl(GlobalEcoNewsController globalEcoNewsController) {
        this.globalEcoNewsController = globalEcoNewsController;
    }

	//신고글 전체 개수
	@Override
	public int reportListsCount() {
		return dao.reportListsCount(sqlSession);
	}

	//검색된 신고글 개수
	@Override
	public int searchReportsCount(HashMap<String, String> map) {
		return dao.searchReportsCount(sqlSession, map);
	}

	//필터링된 신고글 개수
	@Override
	public int filterReportsCount(HashMap<String, String> map) {
		return dao.filterReportsCount(sqlSession, map);
	}

	//신고글 목록 조회
	@Override
	public ArrayList<ReportsVO> reportsList(PageInfo pi) {
		return dao.reportsList(sqlSession, pi);
	}

	//신고글 검색 조회
	@Override
	public ArrayList<ReportsVO> searchReportsList(HashMap<String, String> map, PageInfo pi) {
		return dao.searchReportsList(sqlSession, map, pi);
	}

	//신고글 필터링 조회
	@Override
	public ArrayList<ReportsVO> filterReportsList(HashMap<String, String> map, PageInfo pi) {
		return dao.filterReportsList(sqlSession, map, pi);
	}
	
	//신고글 상세보기
	@Override
	public ReportsVO reportsDetail(int reportsId) {
		return dao.reportsDetail(sqlSession, reportsId);
	}
	
	//신고 등록
	@Transactional
	@Override
	public int reportsInsert(Map<String, Object> map) {
		
		//신고 등록
		int result = dao.reportsInsert(sqlSession, map);
		
		//자동 블라인드 처리
		if (result > 0) {
			int blindResult = reportsBlind(map);
		}
		
		return result;
	}

	//신고 수정
	@Override
	public int reportsUpdate(ReportsVO reports) {
		return dao.reportsUpdate(sqlSession, reports);
	}

	//신고 삭제
	@Override
	public int reportsDelete(ReportsVO reports) {
		return dao.reportsDelete(sqlSession, reports);
	}

	//신고글 상태 처리 - 관리자 권한
	@Transactional
	@Override
	public int reportsStatus(int reportsId, String status) {
		
		int result = dao.reportsStatus(sqlSession, reportsId, status);
	    
	    // 처리완료로 변경한 경우에만 블라인드 체크
	    if(result > 0 && "RESOLVED".equals(status)) {
	        // 해당 신고의 정보 조회
	        ReportsVO report = dao.reportsDetail(sqlSession, reportsId);
	        
	        // Map 생성
	        Map<String, Object> map = new HashMap<>();
	        map.put("type", report.getType());
	        map.put("postId", report.getPostId());
	        map.put("replyId", report.getReplyId());
	        map.put("reviewId", report.getReviewId());
	        
	        // 블라인드 체크
	        reportsBlind(map);
	    }
	    
	    return result;
	}

	//누적 신고 10회 블라인드 처리 
	@Override
	@Transactional
	public int reportsBlind(Map<String, Object> map) {
		int resolvedCount = dao.selectResolvedCount(sqlSession, map);
		
//		if(resolvedCount >= 10) {
//			return dao.reportsBlind(sqlSession, map);
//		}
		
		// 테스트용 -- 누적 신고 수 2회
		if (resolvedCount >= 2) {
			int updateResult = dao.reportsBlind(sqlSession, map);
			return updateResult;
		}
		return 0;
	}

	@Override
	public int reportsCheck(int memberId, int targetMemberId, int postId, int replyId, int reviewId) {
		HashMap<String,Integer> map = new HashMap<>();
		map.put("memberId", memberId);
		map.put("targetMemberId", targetMemberId);
		map.put("postId",postId);
		map.put("replyId",replyId);
		map.put("reviewId",reviewId);

		int result = dao.reportsCheck(sqlSession, map);
		return result;
	}
}
