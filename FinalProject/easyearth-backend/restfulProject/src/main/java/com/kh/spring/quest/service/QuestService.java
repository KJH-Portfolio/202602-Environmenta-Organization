package com.kh.spring.quest.service;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.kh.spring.quest.model.dao.QuestMapper;
import com.kh.spring.quest.model.vo.Quest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestService {

    private final QuestMapper questMapper;
    private final com.kh.spring.common.mapper.HistoryMapper historyMapper;

    /**
     * 오늘의 퀘스트 목록 조회 (INTEGRATED_HISTORY 기반)
     * 오늘 할당이 없으면 5개 랜덤 할당 후 저장, 있으면 저장된 목록 반환
     */
    public List<Quest> getDailyQuests(int userId) {

        if (userId == 0) {
            // 비로그인 시 랜덤 5개 단순 반환 (저장 X)
            List<Quest> all = questMapper.selectDailyQuests();
            if (all != null && !all.isEmpty()) {
                Collections.shuffle(all);
                return all.size() > 5 ? all.subList(0, 5) : all;
            }
            return Collections.emptyList();
        }

        // 1. 오늘 할당된 퀘스트가 있는지 확인
        int assignedCount = historyMapper.countTodayAssignment(userId, "QUEST");

        // 2. 없으면 새로 할당 (랜덤 5개 선정 후 DB 저장)
        if (assignedCount == 0) {
            assignTodaysQuests(userId);
        }

        // 3. 할당된 퀘스트 목록 조회 (완료 여부 포함)
        return historyMapper.selectDailyQuestsWithStatus(userId);
    }

    // 오늘의 퀘스트 5개 랜덤 할당 및 저장
    private void assignTodaysQuests(int userId) {
        List<Quest> allQuests = questMapper.selectDailyQuests();
        if (allQuests != null && !allQuests.isEmpty()) {
            Collections.shuffle(allQuests);
            List<Quest> selected = allQuests.size() > 5 ? allQuests.subList(0, 5) : allQuests;

            for (Quest q : selected) {
                com.kh.spring.common.model.vo.IntegratedHistory history = com.kh.spring.common.model.vo.IntegratedHistory
                        .builder()
                        .memberId(userId)
                        .activityType("QUEST") // 구분값
                        .refId(q.getQuestNo())
                        .build(); // status 기본값 P(Pending)

                historyMapper.insertHistory(history);
            }
        }
    }

    @Transactional
    public void certifyQuest(int userId, int questNo, MultipartFile file) {

        // 1. 오늘 이미 해당 퀘스트를 완료했는지 확인 (INTEGRATED_HISTORY 기준)
        int completed = historyMapper.checkQuestCompleted(userId, questNo);
        if (completed > 0) {
            throw new RuntimeException("오늘 이미 인증한 퀘스트입니다.");
        }

        // 2. 퀘스트 정보 조회 (포인트 확인용)
        Quest quest = questMapper.selectQuestByNo(questNo);
        if (quest == null) {
            throw new RuntimeException("존재하지 않는 퀘스트입니다.");
        }

        // 3. 파일 저장 로직 (필요 시 구현, 현재는 URL 모의 처리)
        // String savedFileName = fileService.upload(file); // 추후 연동
        String savedFileName = "quest_cert_" + System.currentTimeMillis() + ".jpg";

        // 4. 통합 이력 상태 업데이트 (Pending -> Y) 및 이미지 URL 저장
        // (만약 할당되지 않은 퀘스트를 인증하려 하면 update rows가 0이 됨 -> 예외 처리)
        int updated = historyMapper.updateHistoryStatus(userId, "QUEST", questNo, "Y", savedFileName);

        if (updated == 0) {
            // 할당되지 않은 퀘스트를 인증 시도한 경우 (혹은 이미 완료됨)
            // 방어 로직: 억지로라도 Insert를 해야하나? -> 아니오, 할당된 것만 수행 가능해야 함.
            throw new RuntimeException("오늘 할당된 퀘스트가 아니거나 이미 처리되었습니다.");
        }

        // 5. 포인트 지급 (POINT_WALLET 업데이트)
        int pointResult = questMapper.updateMemberPoints(userId, quest.getPoint());
        if (pointResult <= 0) {
            throw new RuntimeException("포인트 지급 실패");
        }

        // (구) QUEST_HISTORY 테이블도 유지하고 싶다면 여기서 insertQuestHistory 호출
        // 하지만 통합 이력으로 이관하므로 생략하거나, 레거시 호환용으로 남길 수 있음.
        // 현재는 통합 이력만 사용.
    }
}
