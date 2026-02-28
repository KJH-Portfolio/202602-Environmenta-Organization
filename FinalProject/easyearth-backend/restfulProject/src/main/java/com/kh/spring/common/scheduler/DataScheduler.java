package com.kh.spring.common.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.kh.spring.global.model.service.GlobalEcoNewsService;

import com.kh.spring.weather.service.WeatherService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DataScheduler {

    @Autowired
    private WeatherService weatherService;

    @Autowired
    private GlobalEcoNewsService globalEcoNewsService;



    @Autowired
    private com.kh.spring.gemini.service.GeminiService geminiService;

    // 1. 날씨 데이터: 매 시간 30분에 갱신 (API가 매 시간 10분쯤 나오므로 30분이 안전)
    @Scheduled(cron = "0 30 * * * *")
    public void scheduleWeatherUpdate() {
        log.info("[Scheduler] 날씨 데이터 정기 갱신 시작");
        weatherService.refreshAllWeatherData();
        
        // 날씨 갱신 후 비서 멘트도 같이 갱신 (새로운 날씨 반영)
        java.util.Map<String, Object> weatherData = weatherService.getCheckWeather();
        geminiService.refreshSecretaryAdvice(weatherData);
        
        log.info("[Scheduler] 날씨 및 비서 멘트 정기 갱신 종료");
    }

    // 2. 뉴스 데이터: 매일 아침 8시, 오후 6시에 갱신 (너무 자주는 필요 없음)
    @Scheduled(cron = "0 0 8,18 * * *")
    public void scheduleNewsUpdate() {
        log.info("[Scheduler] 뉴스 데이터 정기 갱신 시작");
        globalEcoNewsService.refreshGlobalNews();
        log.info("[Scheduler] 뉴스 데이터 정기 갱신 종료");
    }
    
    // 서버 시작 시 최초 1회 실행을 위한 메소드 (ApplicationRunner 등에서 호출 가능하지만, 여기선 생략하고 필요시 컨트롤러로 호출)
}
