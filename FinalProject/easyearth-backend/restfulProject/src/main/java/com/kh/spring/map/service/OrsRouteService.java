package com.kh.spring.map.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrsRouteService {

    @Value("${ors.api.key:eyJvcmciOiI1YjNjZTM1OTc4NTExMTAwMDFjZjYyNDgiLCJpZCI6IjIzNDhlZDgxMzBjOTRjZTdiNWNhMmNjOGRkYmE5MTkwIiwiaCI6Im11cm11cjY0In0=}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> getRouteWithEcoInfo(Double startX, Double startY, Double goalX, Double goalY, String mode) {
        // ORS API 호출 URL
        String url = String.format(
                "https://api.openrouteservice.org/v2/directions/%s?api_key=%s&start=%f,%f&end=%f,%f",
                mode, apiKey, startX, startY, goalX, goalY
        );

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            List features = (List) response.get("features");
            Map firstFeature = (Map) features.get(0);

            // 1. 요약 정보 추출 (거리, 시간)
            Map summary = (Map) ((Map) firstFeature.get("properties")).get("summary");
            // 2. 경로 좌표 추출 (지도에 그릴 선 데이터)
            Map geometry = (Map) firstFeature.get("geometry");

            double distanceMeters = Double.parseDouble(summary.get("distance").toString());
            double durationSeconds = Double.parseDouble(summary.get("duration").toString());

            double distanceKm = distanceMeters / 1000.0;
            long durationMinutes = Math.round(durationSeconds / 60);

            // 환경 수치 계산 로직 실행
            Map<String, Object> result = calculateEcoMetrics(distanceKm, durationMinutes, mode);

            // 프론트에서 지도 그릴 때 쓸 좌표 리스트 추가
            result.put("geometry", geometry.get("coordinates"));

            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("error", "경로 데이터를 가져오는 중 오류가 발생했습니다.");
        }
    }

    private Map<String, Object> calculateEcoMetrics(double distanceKm, long minutes, String mode) {
        double co2Saved = 0;
        double treeEffect = 0;

        // 환경 컨셉: 자동차(driving-car) 대비 절약 수치 계산
        // 도보나 자전거일 때만 절약 수치를 계산합니다.
        if (mode.equals("foot-walking") || mode.equals("cycling-regular")) {
            co2Saved = distanceKm * 0.21; // 중형차 평균 탄소 배출량 210g/km 가정
            treeEffect = co2Saved / 6.6;  // 소나무 한 그루 연간 흡수량 6.6kg 기준 (단순화)
        }

        Map<String, Object> result = new HashMap<>();
        result.put("mode", mode);
        result.put("distanceKm", String.format("%.2f", distanceKm));
        result.put("durationMinutes", minutes);
        result.put("co2Saved", String.format("%.3f", co2Saved));
        result.put("treeEffect", String.format("%.2f", treeEffect));

        return result;
    }
}