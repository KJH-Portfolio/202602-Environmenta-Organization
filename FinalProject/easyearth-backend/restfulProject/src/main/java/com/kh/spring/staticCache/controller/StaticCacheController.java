package com.kh.spring.staticCache.controller;

import com.kh.spring.staticCache.model.StaticCacheService;
import com.kh.spring.staticCache.model.StaticCacheVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/static")
@Slf4j
@Tag(name = "통계", description = "통계")
public class StaticCacheController {

    @Autowired
    public StaticCacheService service;

    @Operation(summary = "개인 효과" , description = "개인 효과")
    @GetMapping("/effects/personal/{memberId}")
    public ResponseEntity<?> environmentEffectPersonal(@PathVariable int memberId) {

        StaticCacheVO vo = service.environmentEffectPersonal(memberId);
        if (vo != null) {
            vo.setMemberId(memberId);
        } else {
            vo = StaticCacheVO.builder().memberId(memberId).co2(0).tree(0).build();
        }
        return ResponseEntity.ok(vo);
    }
    @Operation(summary = "전체 효과", description = "서비스 전체 사용자의 누적 효과")
    @GetMapping("/effects/global")
    public ResponseEntity<?> environmentEffectGlobal() {
        StaticCacheVO globalVo = service.environmentEffectGlobal();
        return ResponseEntity.ok(globalVo);
    }
}
