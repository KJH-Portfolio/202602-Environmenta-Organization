package com.kh.spring.ecoshop.dao;

import com.kh.spring.ecoshop.vo.EcoShop;
import com.kh.spring.ecoshop.vo.Review;
import com.kh.spring.ecoshop.vo.ReviewerName;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class EcoShopDao {


    public List<ReviewerName> reviewList(SqlSessionTemplate sqlSession, int shopId) {
        return sqlSession.selectList("ecoShopMapper.reviewList", shopId);
    }
    public Long getEscIdByThemeId(SqlSessionTemplate sqlSession, String themeId) {
        return sqlSession.selectOne("ecoShopMapper.getEscIdByThemeId", themeId);
    }

    public int checkDuplicate(SqlSessionTemplate sqlSession, String contsId) {
        return sqlSession.selectOne("ecoShopMapper.checkDuplicate", contsId);
    }

    public int insertEcoShop(SqlSessionTemplate sqlSession, EcoShop ecoShop) {
        return sqlSession.insert("ecoShopMapper.insertEcoShop", ecoShop);
    }

    public int reviewInsert(SqlSessionTemplate sqlSession, Review review) {
        return sqlSession.insert("ecoShopMapper.reviewInsert", review);
    }
    public Review boardDetail(SqlSessionTemplate sqlSession, int esrId) {
        return sqlSession.selectOne("ecoShopMapper.reviewDetail",esrId);
    }

    public int reviewDelete(SqlSessionTemplate sqlSession, int esrId) {
        return sqlSession.delete("ecoShopMapper.reviewDelete", esrId);
    }

    public int reviewUpdate(SqlSessionTemplate sqlSession, Review review) {
        return sqlSession.update("ecoShopMapper.reviewUpdate", review);
    }
    public double getAverageRating(SqlSessionTemplate sqlSession, String contsId) {
        return sqlSession.selectOne("ecoShopMapper.getAverageRating", contsId);
    }
    public int getReviewCount(SqlSessionTemplate sqlSession, String contsId) {
        return sqlSession.selectOne("ecoShopMapper.getReviewCount", contsId);
    }

    public int findShopIdByContsId(SqlSessionTemplate sqlSession, String contsId) {
        Integer result = sqlSession.selectOne("ecoShopMapper.findShopIdByContsId", contsId);

        // 2. 결과가 null이면 0을, 있으면 그 값을 반환합니다.
        return (result == null) ? 0 : result;
    }

}