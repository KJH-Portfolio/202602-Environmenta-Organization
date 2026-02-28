package com.kh.spring.weather.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kh.spring.weather.service.WeatherService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "날씨 API 조회")
@RequestMapping("/weather")
public class WeatherController {

    @Autowired
    private WeatherService weatherService;

    @Autowired
    private com.kh.spring.common.scheduler.DataScheduler dataScheduler;

    // 일기예보 조회 (단기/중기)
    @Operation(summary = "일기예보 조회")
    @GetMapping("/forecast")
    public ResponseEntity<?> getForecast(){

        return ResponseEntity.ok(weatherService.getForecastList());

    }

    // 기상 관측 자료 조회 (ASOS/AWS)
    @Operation(summary = "기상 관측 자료 조회")
    @GetMapping("/obs")
    public ResponseEntity<?> getObs(){

        return ResponseEntity.ok(weatherService.getObsList());

    }

    // 미세먼지/황사 조회
    @Operation(summary = "황사/미세먼지 조회")
    @GetMapping("/dust")
    public ResponseEntity<?> getDust() {

        return ResponseEntity.ok(weatherService.getDustList());

    }

    // 자외선 지수 조회
    @Operation(summary = "자외선 지수 조회")
    @GetMapping("/uv")
    public ResponseEntity<?> getUv() {

        return ResponseEntity.ok(weatherService.getUvList());

    }
    
    // 전체 날씨 데이터 조회 (테스트용)
    @Operation(summary = "전체 데이터 조회 (테스트)")
    @GetMapping("/allData")
    public ResponseEntity<?> getData() {
    	return ResponseEntity.ok(weatherService.getCheckWeather());
    }
    
    // 날씨/뉴스 캐시 강제 갱신 (스케줄러 수동 실행)
    @Operation(summary = "캐시 강제 갱신")
    @GetMapping("/refresh")
    public ResponseEntity<?> refreshCache() {
        dataScheduler.scheduleWeatherUpdate();
        dataScheduler.scheduleNewsUpdate();
        return ResponseEntity.ok("Cache Refreshed");
    }

}
