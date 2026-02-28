package com.kh.spring.staticCache.model;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class StaticCacheDao {

    public StaticCacheVO environmentEffectPersonal(SqlSessionTemplate sqlSession, int memberId) {
        return sqlSession.selectOne("staticCacheMapper.environmentEffectPersonal", memberId);
    }

    public StaticCacheVO environmentEffectGlobal(SqlSessionTemplate sqlSession) {
        return sqlSession.selectOne("staticCacheMapper.environmentEffectGlobal");
    }
}
