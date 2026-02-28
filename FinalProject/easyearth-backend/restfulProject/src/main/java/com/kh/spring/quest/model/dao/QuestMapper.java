package com.kh.spring.quest.model.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.kh.spring.quest.model.vo.Quest;

@Mapper
public interface QuestMapper {

        // 오늘의 퀘스트 목록 조회
        List<Quest> selectDailyQuests();

        // 특정 퀘스트 정보 조회
        Quest selectQuestByNo(int questNo);

        // 퀘스트 인증 내역 저장 (QUEST_HISTORY)
        int insertQuestHistory(@org.apache.ibatis.annotations.Param("userId") int userId,
                        @org.apache.ibatis.annotations.Param("questNo") int questNo,
                        @org.apache.ibatis.annotations.Param("savedFileName") String savedFileName);

        int countTodayQuests(@org.apache.ibatis.annotations.Param("userId") int userId);

        // 포인트 지급
        int updateMemberPoints(@org.apache.ibatis.annotations.Param("userId") int userId,
                        @org.apache.ibatis.annotations.Param("points") int points);

        // 오늘 특정 퀘스트 인증 여부 확인
        int countTodayQuestByNo(@org.apache.ibatis.annotations.Param("userId") int userId,
                        @org.apache.ibatis.annotations.Param("questNo") int questNo);
}
