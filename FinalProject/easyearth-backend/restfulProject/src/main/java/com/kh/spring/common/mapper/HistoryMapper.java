package com.kh.spring.common.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.kh.spring.common.model.vo.IntegratedHistory;
import com.kh.spring.quiz.model.vo.Quiz;
import com.kh.spring.quest.model.vo.Quest;

@Mapper
public interface HistoryMapper {

    // 오늘 할당된 이력 개수 조회
    int countTodayAssignment(@Param("memberId") int memberId, @Param("typePrefix") String typePrefix);

    // 통합 이력 인서트
    int insertHistory(IntegratedHistory history);

    // 오늘의 대기중인 퀴즈 목록 조회
    List<Quiz> selectPendingQuizzes(@Param("memberId") int memberId, @Param("activityType") String activityType);

    // 오늘의 퀘스트 목록 조회
    List<Quest> selectDailyQuestsWithStatus(@Param("memberId") int memberId);

    // 활동 상태 및 이미지 업데이트
    int updateHistoryStatus(@Param("memberId") int memberId,
            @Param("activityType") String activityType,
            @Param("refId") int refId,
            @Param("status") String status,
            @Param("imageUrl") String imageUrl);

    // 자정 정기 삭제 (Pending 데이터 정리)
    int deleteYesterdayPending();

    // 퀘스트 완료 여부 확인
    int checkQuestCompleted(@Param("memberId") int memberId, @Param("questNo") int questNo);

    // 오늘의 미해결 퀴즈 개수 조회
    int countPendingQuizzes(@Param("memberId") int memberId, @Param("activityType") String activityType);
}
