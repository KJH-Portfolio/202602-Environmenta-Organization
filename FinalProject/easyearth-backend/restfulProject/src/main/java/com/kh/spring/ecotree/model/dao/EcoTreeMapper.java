package com.kh.spring.ecotree.model.dao;

import org.apache.ibatis.annotations.Mapper;
import com.kh.spring.ecotree.model.vo.EcoTreeVO;

@Mapper
public interface EcoTreeMapper {
    /**
     * 회원의 나무 정보 조회 (Wallet의 누적 포인트 포함)
     */
    EcoTreeVO selectTreeByMemberId(int memberId);

    /**
     * 나무 정보 초기 생성
     */
    int insertTree(int memberId);

    /**
     * 나무 성장 정보 업데이트 (반영된 EXP 및 레벨)
     */
    int updateTreeGrowth(EcoTreeVO tree);
}
