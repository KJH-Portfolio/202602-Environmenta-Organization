package com.kh.spring.staticCache.model;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StaticCacheService {

    @Autowired
    private StaticCacheDao dao;

    @Autowired
    private SqlSessionTemplate sqlSession;

    // 개인 통계 (정답률 포함)
    public StaticCacheVO environmentEffectPersonal(int memberId) {
        StaticCacheVO vo = dao.environmentEffectPersonal(sqlSession, memberId);
        if (vo != null) {
            int success = vo.getQuizSuccessCount();
            int fail = vo.getQuizFailCount();
            int total = success + fail;

            if (total > 0) {
                double rate = ((double) success / total) * 100;
                vo.setQuizRate(Math.round(rate * 100) / 100.0);
            }
            vo.setMemberId(memberId);
        }
        return vo;
    }

    // 전체 통계 (CO2, Tree만 반환)
    public StaticCacheVO environmentEffectGlobal() {
        return dao.environmentEffectGlobal(sqlSession);
    }
}
