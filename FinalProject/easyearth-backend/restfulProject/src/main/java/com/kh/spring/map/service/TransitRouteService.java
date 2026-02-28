package com.kh.spring.map.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TransitRouteService {

    @Value("${odsay.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> getTransitRoute(Double startX, Double startY, Double goalX, Double goalY) {
        // 1. 경로 검색 API URL 구성
        String searchUrl = UriComponentsBuilder.fromHttpUrl("https://api.odsay.com/v1/api/searchPubTransPathT")
                .queryParam("SX", startX)
                .queryParam("SY", startY)
                .queryParam("EX", goalX)
                .queryParam("EY", goalY)
                .queryParam("apiKey", apiKey)
                .toUriString();

        try {
            Map<String, Object> response = restTemplate.getForObject(searchUrl, Map.class);

            if (response == null || response.get("result") == null) {
                return Map.of("error", "검색 결과가 없거나 API 키 설정이 잘못되었습니다.");
            }

            Map resultBody = (Map) response.get("result");
            List pathList = (List) resultBody.get("path");

            // 2. 가장 최적의 경로(첫 번째) 가져오기
            Map bestPath = (Map) pathList.get(0);
            Map info = (Map) bestPath.get("info");

            // 기본 정보 추출
            double distanceKm = Double.parseDouble(info.get("totalDistance").toString()) / 1000.0;
            int totalTime = Integer.parseInt(info.get("totalTime").toString());
            int payment = Integer.parseInt(info.get("payment").toString());

            // 3. 방법 B: 상세 경로 그래픽(Polyline) 데이터 가져오기 추가
            String mapObj = info.get("mapObj").toString();
            String graphicUrl = UriComponentsBuilder.fromHttpUrl("https://api.odsay.com/v1/api/loadLaneGraphic")
                    .queryParam("mapObject", "0:0@" + mapObj)
                    .queryParam("apiKey", apiKey)
                    .toUriString();

            Map<String, Object> graphicResponse = restTemplate.getForObject(graphicUrl, Map.class);
            Object polylineData = (graphicResponse != null) ? graphicResponse.get("result") : null;

            // 4. 환경 수치 계산 및 결과 조립
            Map<String, Object> result = calculateEcoMetrics(distanceKm, totalTime);
            result.put("payment", payment);
            result.put("transitCount", info.get("busTransitCount") + " (버스) / " + info.get("subwayTransitCount") + " (지하철)");
            result.put("subPaths", bestPath.get("subPath"));

            // 지도용 상세 좌표 데이터 추가
            result.put("polyline", polylineData);

            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("error", "대중교통 데이터를 가져오는 중 서버 오류가 발생했습니다.");
        }
    }

    private Map<String, Object> calculateEcoMetrics(double distanceKm, int minutes) {
        double co2Saved = distanceKm * 0.168;
        double treeEffect = co2Saved / 6.6;

        Map<String, Object> result = new HashMap<>();
        result.put("mode", "public-transit");
        result.put("distanceKm", String.format("%.2f", distanceKm));
        result.put("durationMinutes", minutes);
        result.put("co2Saved", String.format("%.3f", co2Saved));
        result.put("treeEffect", String.format("%.2f", treeEffect));
        return result;
    }
}