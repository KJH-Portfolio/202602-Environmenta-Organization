package com.kh.spring.ecoshop.service;

import com.kh.spring.ecoshop.dao.EcoShopDao;
import com.kh.spring.ecoshop.vo.EcoShop;
import com.kh.spring.ecoshop.vo.Review;
import com.kh.spring.ecoshop.vo.ReviewerName;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EcoShopService {

    @Autowired
    private EcoShopDao ecoShopDao;

    @Autowired
    private SqlSessionTemplate sqlSession;

    public List<ReviewerName> reviewList(int shopId) {
        return ecoShopDao.reviewList(sqlSession, shopId);
    }
    public Long findEscId(String themeId) {
        return ecoShopDao.getEscIdByThemeId(sqlSession, themeId);
    }

    @Transactional
    public int insertEcoShop(EcoShop ecoShop) {
        int count = ecoShopDao.checkDuplicate(sqlSession, ecoShop.getContsId());
        if (count == 0) {
            return ecoShopDao.insertEcoShop(sqlSession, ecoShop);
        }
        return 0;
    }

    public int reviewInsert(Review review) {
        return ecoShopDao.reviewInsert(sqlSession, review);
    }

    public Review reviewDetail(int esrId) {
        return ecoShopDao.boardDetail(sqlSession,esrId);
    }

    public int reviewDelete(int esrId) {
        return ecoShopDao.reviewDelete(sqlSession,esrId);
    }

    public int reviewUpdate(Review review) {
        return ecoShopDao.reviewUpdate(sqlSession, review);
    }

    public double getAverageRating(String contsId) {
        return ecoShopDao.getAverageRating(sqlSession, contsId);
    }
    public int getReviewCount(String contsId) {
        return ecoShopDao.getReviewCount(sqlSession, contsId);
    }
    public int findShopIdByContsId(String contsId) {
        return ecoShopDao.findShopIdByContsId(sqlSession, contsId);
    }
}