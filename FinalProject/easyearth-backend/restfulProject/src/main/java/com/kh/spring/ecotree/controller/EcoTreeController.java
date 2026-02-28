package com.kh.spring.ecotree.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kh.spring.ecotree.model.service.EcoTreeService;
import com.kh.spring.ecotree.model.vo.EcoTreeVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/ecotree")
@RequiredArgsConstructor
@Tag(name = "에코트리", description = "누적 포인트를 이용한 나무 성장 게임 API")
public class EcoTreeController {

    private final EcoTreeService ecoTreeService;

    @Operation(summary = "나무 상태 조회", description = "회원의 현재 나무 정보와 지갑의 총 누적 포인트를 조회합니다.")
    @GetMapping("/{memberId}")
    public ResponseEntity<EcoTreeVO> getTreeInfo(@PathVariable int memberId) {
        EcoTreeVO tree = ecoTreeService.getTreeInfo(memberId);
        if (tree != null) {
            return ResponseEntity.ok(tree);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "나무 성장 시키기", description = "현재 지갑의 누적 포인트를 나무 경험치로 반영하여 레벨업을 확인합니다.")
    @PostMapping("/grow/{memberId}")
    public ResponseEntity<EcoTreeVO> growTree(@PathVariable int memberId) {
        EcoTreeVO updatedTree = ecoTreeService.growTree(memberId);
        if (updatedTree != null) {
            return ResponseEntity.ok(updatedTree);
        } else {
            return ResponseEntity.internalServerError().build();
        }
    }
}
