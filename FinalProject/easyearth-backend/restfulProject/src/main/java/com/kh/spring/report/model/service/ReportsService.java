package com.kh.spring.report.model.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.kh.spring.common.model.vo.PageInfo;
import com.kh.spring.report.model.vo.ReportsVO;

public interface ReportsService {

	//신고글 전체 개수
	int reportListsCount();
	
	//검색된 신고글 개수
	int searchReportsCount(HashMap<String, String> map);

	//필터링된 신고글 개수
	int filterReportsCount(HashMap<String, String> map);
	
	//신고글 목록 조회
	ArrayList<ReportsVO> reportsList(PageInfo pi);

	//신고글 검색 조회
	ArrayList<ReportsVO> searchReportsList(HashMap<String, String> map, PageInfo pi);

	//신고글 필터링 조회
	ArrayList<ReportsVO> filterReportsList(HashMap<String, String> map, PageInfo pi);
	
	//신고글 상세보기
	ReportsVO reportsDetail(int reportsId);
	
	//신고 등록
	int reportsInsert(Map<String, Object> map);

	//신고 수정
	int reportsUpdate(ReportsVO reports);

	//신고 삭제
	int reportsDelete(ReportsVO reports);

	//신고글 상태 처리 - 관리자 권한
	int reportsStatus(int reportsId, String status);

	//누적 신고 10회 블라인드 처리 
	int reportsBlind(Map<String, Object> map);


    int reportsCheck(int memberId, int targetMemberId, int postId, int replyId, int reviewId);


}
