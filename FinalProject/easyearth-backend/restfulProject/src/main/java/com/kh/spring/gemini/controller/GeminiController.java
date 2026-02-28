package com.kh.spring.gemini.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kh.spring.gemini.service.GeminiService;
import com.kh.spring.weather.service.WeatherService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;


// 재미나이에게 질문하고 답변 받는 클래스(하드코딩)
@RestController
@Tag(name = "재미나이 기능")
@RequestMapping("/gemini")
public class GeminiController {

    @Autowired
    private GeminiService geminiService;
    
    @Autowired
    private WeatherService weatherService;
    
    @Operation(summary = "재미나이 성능 테스트")
    @PostMapping("/onlyTest")
    public String onlyTest(String question) {
    	String prompt = ""; 
    	String response = geminiService.custom(question,prompt);
    	
    	return response;
    }
    
    @Operation(summary="분리수거 도우미",description = "분리수거 할 제품 입력")
    @PostMapping("/Recycling")
    public String recycling(String question) {
    	String prompt = "을 분리수거 하는 법 서울시 2025년 6월 분리배출 표준안(최신) 기준으로 알려줘";
    	String response = geminiService.custom(question, prompt);
    	
    	return response;
    }

    @Operation(summary = "환경 비서에게 데이터 입력하여 날씨+환경 관련 멘트 받기")
    @PostMapping("/secretary")
    public Map<String, String> getSecretaryAdvice() {
        // 1. 모든 날씨 데이터 수집
        Map<String, Object> weatherData = weatherService.getCheckWeather();

        System.out.println(weatherData);

        // 2. Gemini에게 조언 요청 (캐시 적용)
        String advice = geminiService.getSecretaryAdvice(weatherData);

        // 3. 응답 반환
        Map<String, String> response = new HashMap<>();
        response.put("message", advice);
        return response;
    }

}