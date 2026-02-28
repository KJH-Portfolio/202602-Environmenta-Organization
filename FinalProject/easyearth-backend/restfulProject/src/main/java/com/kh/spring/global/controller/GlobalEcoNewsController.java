package com.kh.spring.global.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/global")
@Tag(name = "글로벌 환경 뉴스", description = "NYT RSS + Gemini 요약")
public class GlobalEcoNewsController {

    @Autowired
    private com.kh.spring.global.model.service.GlobalEcoNewsService globalEcoNewsService;

    // 글로벌 환경 뉴스 조회 (캐시)
    @Operation(summary = "글로벌 환경 뉴스 조회")
    @GetMapping(value = "/news", produces = "application/json; charset=UTF-8")
    public ResponseEntity<String> getGlobalNews() {
        return ResponseEntity.ok(globalEcoNewsService.getGlobalEcoNews());
    }

    // 뉴스 캐시 강제 갱신
    @Operation(summary = "뉴스 캐시 강제 갱신")
    @GetMapping("/refresh")
    public ResponseEntity<String> refreshNews() {
        return ResponseEntity.ok(globalEcoNewsService.refreshGlobalNews());
    }
    
    // 뉴스 캐시 파일 경로는 로컬에서 관리됨
    @Operation(summary = "캐시 관리 (로컬)")
    @GetMapping("/clear-cache")
    public ResponseEntity<String> clearCache() {
        // 파일 기반 캐시라 특별히 할 건 없지만, 필요하면 파일 삭제 로직 추가 가능
        return ResponseEntity.ok("Cache management delegated to File System.");
    }
}
