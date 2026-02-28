package com.kh.spring.community.model.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.kh.spring.common.model.vo.PageInfo;
import com.kh.spring.community.model.vo.CommunityPostVO;
import com.kh.spring.community.model.vo.CommunityReplyVO;
import com.kh.spring.community.model.vo.PostFilesVO;

public interface CommunityService {
	
	//게시글 총 개수
	int listCount();
	
	//검색된 게시글 개수
	int searchListCount(HashMap<String, String> map);

	//필터링된 게시글 개수
	int filterListCount(HashMap<String, String> map);

	//게시글 목록 조회
	ArrayList<CommunityPostVO> communityList(PageInfo pi);
	
	//게시글 검색 조회
	ArrayList<CommunityPostVO> searchList(HashMap<String, String> map, PageInfo pi);

	//게시글 필터링 조회
	ArrayList<CommunityPostVO> filterList(HashMap<String, String> map, PageInfo pi);
	
	//게시글 상세보기
	CommunityPostVO communityDetail(int postId);
	
	//게시글 등록
	int communityInsert(CommunityPostVO cp, ArrayList<PostFilesVO> pfList);

	//게시글 수정 - 기존 파일 삭제
	ArrayList<PostFilesVO> selectFilesByIds(int postId, ArrayList<Integer> delFileIds);

	//게시글 수정
	int communityUpdate(CommunityPostVO cp, ArrayList<PostFilesVO> newPfList, ArrayList<Integer> delFileIds);

	//게시글 첨부파일 목록 조회
	ArrayList<PostFilesVO> selectFilesByPostIds(int postId);

	//게시글 삭제
	int communityDelete(int postId);

	//게시글 조회수 증가
	int increaseViewCount(int postId);

    //댓글 목록 조회
	ArrayList<CommunityReplyVO> replyList(int postId);
	
	//댓글 등록
	int replyInsert(CommunityReplyVO reply);

	//댓글 수정
	int replyUpdate(CommunityReplyVO reply);

	//댓글 삭제
	int replyDelete(CommunityReplyVO reply);

	//게시글 좋아요
	String communityLikes(Map<String, Object> map);
	
	//게시글 좋아요 상태 조회
	String getPostLikeStatus(Map<String, Object> map);

	//댓글 좋아요
	String replyLikes(Map<String, Object> map);

	//댓글 좋아요 상태 조회
	String getReplyLikeStatus(Map<String, Object> map);


	







}
