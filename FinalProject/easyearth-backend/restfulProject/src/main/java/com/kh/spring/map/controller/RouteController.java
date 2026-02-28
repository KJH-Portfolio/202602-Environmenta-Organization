package com.kh.spring.map.controller;

import com.kh.spring.map.service.OrsRouteService;
import com.kh.spring.map.service.TransitRouteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Tag(name="길찾기 API", description = "길찾기 API ")
@RequestMapping("/api/route")
@RequiredArgsConstructor
public class RouteController {

    private final OrsRouteService orsRouteService;
    private final TransitRouteService transitRouteService;
    @Operation(summary = "ORS API", description = "자동차 : driving-car / 도보 : foot-walking / 자전거 : cycling-regular")
    @GetMapping("/ors")
    public ResponseEntity<?> getOrsRoute(
            @RequestParam(defaultValue = "126.974695") Double startX,
            @RequestParam(defaultValue = "37.564149") Double startY,
            @RequestParam(defaultValue ="127.106928082") Double goalX,
            @RequestParam(defaultValue = "37.580981471") Double goalY,
            @RequestParam(defaultValue = "driving-car") String mode) {

        Map<String, Object> result = orsRouteService.getRouteWithEcoInfo(startX, startY, goalX, goalY, mode);

        if (result.containsKey("error")) {
            return ResponseEntity.internalServerError().body(result.get("정보를 불러오는데 오류가 발생했습니다."));
        }

        return ResponseEntity.ok(result);
    }
    @GetMapping("/transit")
    @Operation(summary = "ODsay API", description = ".")
    public ResponseEntity<?> getTransitRoute(
            @RequestParam(defaultValue = "126.974695") Double startX,
            @RequestParam(defaultValue = "37.564149") Double startY,
            @RequestParam(defaultValue ="127.106928082") Double goalX,
            @RequestParam(defaultValue = "37.580981471") Double goalY) {

        Map<String, Object> result = transitRouteService.getTransitRoute(startX, startY, goalX, goalY);

        if (result.containsKey("error")) {
            return ResponseEntity.badRequest().body("정보를 불러오는데 오류가 발생했습니다.");
        }

        return ResponseEntity.ok(result);
    }
}
