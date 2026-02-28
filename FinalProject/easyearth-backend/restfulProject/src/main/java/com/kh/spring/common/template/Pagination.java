package com.kh.spring.common.template;

import com.kh.spring.common.model.vo.PageInfo;

public class Pagination {
	
	public static PageInfo getPageInfo(int listCount, int currentPage
									  ,int boardLimit, int pageLimit) {
		
		int maxPage = (int)Math.ceil((double)listCount / boardLimit);
		int startPage = (currentPage - 1) / pageLimit * pageLimit + 1;
		int endPage = startPage + pageLimit - 1;
		
		if(maxPage < endPage) {
			endPage = maxPage;
		}
		
		PageInfo pi = PageInfo.builder()
							  .listCount(listCount)
							  .currentPage(currentPage)
							  .boardLimit(boardLimit)
							  .pageLimit(pageLimit)
							  .maxPage(maxPage)
							  .startPage(startPage)
							  .endPage(endPage)
							  .build();
		
		return pi;
	}
}
