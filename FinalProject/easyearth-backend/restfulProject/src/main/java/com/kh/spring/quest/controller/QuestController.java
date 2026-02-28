package com.kh.spring.quest.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.kh.spring.quest.model.vo.Quest;
import com.kh.spring.quest.service.QuestService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/quest")
@RequiredArgsConstructor
public class QuestController {

    private final QuestService questService;

    // 오늘의 데일리 퀘스트 목록 조회 (userId로 완료 여부 포함)
    @GetMapping("/daily")
    public ResponseEntity<List<Quest>> getDailyQuests(
            @RequestParam(value = "userId", defaultValue = "0") int userId) {
        List<Quest> list = questService.getDailyQuests(userId);
        return ResponseEntity.ok(list);
    }

    // 퀘스트 인증 (사진 업로드)
    @PostMapping("/certify/{questNo}")
    public ResponseEntity<String> certifyQuest(
            @PathVariable("questNo") int questNo,
            @RequestParam("userId") int userId,
            @RequestParam("file") MultipartFile file) {

        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body("파일이 없습니다.");
            }

            questService.certifyQuest(userId, questNo, file);
            return ResponseEntity.ok("인증이 완료되었습니다! 포인트가 지급되었습니다.");
        } catch (RuntimeException e) {
            // 중복 인증 등의 에러를 400으로 반환
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Quest Certify Error", e);
            return ResponseEntity.internalServerError().body("인증 처리 중 오류가 발생했습니다.");
        }
    }
}