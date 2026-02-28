package com.kh.spring.ecoshop.controller;


import com.kh.spring.ecoshop.service.EcoShopService;
import com.kh.spring.ecoshop.vo.Review;
import com.kh.spring.ecoshop.vo.ReviewerName;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name="EcoShop 관련 Controller", description = "EcoShop 관련 Controller")
@RequestMapping("/eco")
@Controller
public class EcoShopController {

    @Autowired
    private EcoShopService ecoShopService;


    @GetMapping("/review/list/{shopId}")
    @Operation(summary = "리뷰 List Controller", description = ".")
    public ResponseEntity<?> reviewList(@PathVariable int shopId) {
        List<ReviewerName> list = ecoShopService.reviewList(shopId);
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @PostMapping("/review/write")
    @Operation(summary = "리뷰 등록 Controller" , description = ".")
    public ResponseEntity<?> reviewInsert(Review review) {
        int result = ecoShopService.reviewInsert(review);
        if(result > 0) {
            return ResponseEntity.status(HttpStatus.CREATED).body("리뷰 등록 성공");
        }
        else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("리뷰 등록 실패");
        }

    }

    @DeleteMapping("/review/delete/{esrId}")
    @Operation(summary = "리뷰 삭제 Controller", description = ".")
    public ResponseEntity<?> reviewDelete(@PathVariable int esrId) {

        Review review = ecoShopService.reviewDetail(esrId);
        if(review == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 리뷰를 찾을 수 없습니다.");
        }
        int result = ecoShopService.reviewDelete(esrId);
        if(result>0) {
            return ResponseEntity.status(HttpStatus.CREATED).body("삭제 성공");
        }
        else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("삭제 실패");
        }
    }

    @PutMapping("/review/update")
    @Operation(summary = "리뷰 수정 Controller" , description = ".")
    public ResponseEntity<?> reviewUpdate(@RequestBody Review review) {
        int result = ecoShopService.reviewUpdate(review);
        if(result>0) {
            return ResponseEntity.status(HttpStatus.CREATED).body("수정 성공");
        }
        else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("수정 실패");
        }
    }
}
