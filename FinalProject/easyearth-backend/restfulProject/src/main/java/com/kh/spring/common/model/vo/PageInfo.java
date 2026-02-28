package com.kh.spring.common.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PageInfo {
	private int currentPage;  //현재 페이지
	private int listCount;  //총 게시글 개수
	private int boardLimit;  //게시글 보여줄 개수
	private int pageLimit;  //페이징바 개수
	private int maxPage;  //페이징바 최대 페이지
	private int startPage;  //페이징바 시작 페이지
	private int endPage;  //페이징바 끝 페이지
	
}
