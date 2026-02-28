package com.kh.spring.ecotree.model.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.spring.ecotree.model.dao.EcoTreeMapper;
import com.kh.spring.ecotree.model.vo.EcoTreeVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EcoTreeService {

    private final EcoTreeMapper ecoTreeMapper;

    // 성장 단계별 필요 누적 포인트 (경험치)
    // 6개월(180일) * 일평균 21,000P = 약 3,780,000P 기준
    private static final long LV2_THRESHOLD = 500000L;    // 약 24일차
    private static final long LV3_THRESHOLD = 1500000L;   // 약 71일차
    private static final long LV4_THRESHOLD = 3780000L;   // 약 180일차 (6개월)

    /**
     * 나무 상태 조회 (없으면 자동 생성)
     */
    @Transactional
    public EcoTreeVO getTreeInfo(int memberId) {
        EcoTreeVO tree = ecoTreeMapper.selectTreeByMemberId(memberId);
        
        // 데이터가 없으면 초기 생성 후 다시 조회
        if (tree == null) {
            ecoTreeMapper.insertTree(memberId);
            tree = ecoTreeMapper.selectTreeByMemberId(memberId);
        }
        
        return tree;
    }

    /**
     * 나무 성장 (누적 포인트 반영)
     */
    @Transactional
    public EcoTreeVO growTree(int memberId) {
        // 1. 현재 나무 정보와 지갑의 누적 포인트 조회
        EcoTreeVO tree = ecoTreeMapper.selectTreeByMemberId(memberId);
        
        if (tree == null) {
            return null;
        }

        // 2. 지갑의 누적 포인트를 나무 경험치로 전액 반영
        long currentTotalEarned = tree.getTotalEarnedPoint();
        tree.setSyncedExp(currentTotalEarned);

        // 3. 누적 포인트에 따른 레벨 계산
        int newLevel = calculateLevel(currentTotalEarned);
        tree.setTreeLevel(newLevel);

        // 4. DB 업데이트
        ecoTreeMapper.updateTreeGrowth(tree);

        return tree;
    }

    /**
     * 누적 포인트에 따른 레벨 계산 로직
     */
    private int calculateLevel(long exp) {
        if (exp >= LV4_THRESHOLD) {
            return 4;
        } else if (exp >= LV3_THRESHOLD) {
            return 3;
        } else if (exp >= LV2_THRESHOLD) {
            return 2;
        } else {
            return 1;
        }
    }
}
