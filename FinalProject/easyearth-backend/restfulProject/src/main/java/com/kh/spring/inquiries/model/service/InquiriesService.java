package com.kh.spring.inquiries.model.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.kh.spring.common.model.vo.PageInfo;
import com.kh.spring.inquiries.model.vo.InquiriesVO;

public interface InquiriesService {
	
	//건의글 총 개수
	int listCount();

	//검색된 건의글 개수
	int searchListCount(HashMap<String, Object> map);

	//필터링된 건의글 개수
	int filterListCount(HashMap<String, Object> map);

	//건의글 목록 조회
	ArrayList<InquiriesVO> inquiriesList(PageInfo pi);

	//건의글 검색 조회
	ArrayList<InquiriesVO> searchList(HashMap<String, Object> map, PageInfo pi);

	//건의글 필터링 조회
	ArrayList<InquiriesVO> filterList(HashMap<String, Object> map, PageInfo pi);
	
	//건의글 조회수 증가
	int increaseCount(int inquiriesId);

	//건의글 상세보기
	InquiriesVO inquiriesDetail(int inquiriesId);

	//건의글 등록
	int inquiriesInsert(Map<String, Object> map);

	//건의글 수정
	int inquiriesUpdate(Map<String, Object> map);

	//건의글 삭제
	int inquiriesDelete(Map<String, Object> map);
  
	//건의글 상태 처리 - 관리자 권한
	int inquiriesStatus(int inquiriesId, String status);

	//건의글 답변 - 관리자 권한
	int inquiriesAdmintReply(Map<String, Object> map);
	

}
