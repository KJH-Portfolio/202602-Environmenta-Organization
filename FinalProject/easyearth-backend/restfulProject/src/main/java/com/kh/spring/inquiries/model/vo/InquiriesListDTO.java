package com.kh.spring.inquiries.model.vo;

import java.util.List;

import com.kh.spring.common.model.vo.PageInfo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class InquiriesListDTO {

	private List<InquiriesVO> list;  //게시글 목록
	private int listCount;  //총 게시글 수
	private int currentPage;  //현재 페이지
	private int maxPage;  //마지막 페이지
	private int startPage;  //페이징바 시작 페이지
	private int endPage;  //페이징바 끝 페이지
	
	public static InquiriesListDTO of(List<InquiriesVO> list, PageInfo pageInfo) {
		return InquiriesListDTO.builder()
							   .list(list)
							   .currentPage(pageInfo.getCurrentPage())
							   .listCount(pageInfo.getListCount())
							   .maxPage(pageInfo.getMaxPage())
							   .startPage(pageInfo.getStartPage())
							   .endPage(pageInfo.getEndPage())
							   .build();
	}
}

