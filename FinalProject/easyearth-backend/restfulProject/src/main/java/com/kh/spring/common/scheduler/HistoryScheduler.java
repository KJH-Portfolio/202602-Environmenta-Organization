package com.kh.spring.common.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.kh.spring.common.mapper.HistoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class HistoryScheduler {

    private final HistoryMapper historyMapper;

    /**
     * 매일 자정(00:00)에 실행되어 어제까지 완료되지 않은(Pending) 이력을 삭제합니다.
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void cleanupPendingHistories() {
        log.info("Midnight Integrated History Cleanup Started...");
        try {
            int deletedCount = historyMapper.deleteYesterdayPending();
            log.info("Successfully deleted {} pending histories from yesterday.", deletedCount);
        } catch (Exception e) {
            log.error("Error during midnight history cleanup", e);
        }
    }
}
