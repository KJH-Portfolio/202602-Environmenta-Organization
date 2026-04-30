package com.kh.spring.weather.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;

import com.kh.spring.weather.model.vo.DustDto;
import com.kh.spring.weather.model.vo.ForecastDto;
import com.kh.spring.weather.model.vo.ForecastResult;
import com.kh.spring.weather.model.vo.ObsDto;
import com.kh.spring.weather.model.vo.UvDto;

import org.springframework.web.reactive.function.client.WebClient;

@Service
public class WeatherService {




    @Value("${kma.service.key}")
    private String kmaServiceKey;

    @Value("${kma.auth.key}")
    private String kmaAuthKey;

    @Autowired
    private com.kh.spring.common.service.FileCacheService fileCacheService;
    
    // 캐시 파일명 상수
    private static final String CACHE_FORECAST = "weather_forecast.json";
    private static final String CACHE_OBS = "weather_obs.json";
    private static final String CACHE_DUST = "weather_dust.json";
    private static final String CACHE_UV = "weather_uv.json";

    // --- Public Methods (Cache Read) ---

    // 단기예보 조회 (캐시 우선)
    public List<ForecastDto> getForecastList() {
        // 캐시 확인
        ForecastDto[] cached = fileCacheService.load(CACHE_FORECAST, ForecastDto[].class);
        if (cached != null) {
            return Arrays.asList(cached);
        }
        // 캐시 없으면 바로 갱신 후 반환
        return refreshForecastList();
    }

    // --- Private Fetch Methods & Refresh Logic ---
    
    // 단기예보 API 호출 및 캐시 갱신
    public List<ForecastDto> refreshForecastList() {
        List<ForecastDto> data = fetchForecastList(); // API 호출
        if (!data.isEmpty()) {
            fileCacheService.save(CACHE_FORECAST, data);
        }
        return data;
    }

    // 단기예보 공공데이터 API 데이터 조회
    private List<ForecastDto> fetchForecastList() {
        String serviceKey = kmaServiceKey;
        LocalDateTime now = LocalDateTime.now(); // 현재 시간

        WebClient webClient = WebClient.builder()
                .baseUrl("https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0")
                .build();

        ForecastResult result = null;
        try {
            result = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/getVilageFcst")
                            .queryParam("serviceKey", serviceKey)
                            .queryParam("pageNo", "1")
                            .queryParam("numOfRows", "1000")
                            .queryParam("dataType", "JSON")
                            .queryParam("base_date", getBaseDate(now))
                            .queryParam("base_time", getBaseTime(now))
                            .queryParam("nx", "55")
                            .queryParam("ny", "127")
                            .build())
                    .retrieve()
                    .bodyToMono(ForecastResult.class)
                    .block();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }

        if (result == null || result.getForecastList() == null) {
            return new ArrayList<>();
        }

        // --- 필터링 로직 시작 ---
        return result.getForecastList().stream()
                .filter(dto -> {
                    // fcstDate(20260212)와 fcstTime(0600)을 LocalDateTime으로 변환
                    LocalDateTime forecastTime = LocalDateTime.parse(
                            dto.getFcstDate() + dto.getFcstTime(),
                            DateTimeFormatter.ofPattern("yyyyMMddHHmm")
                    );

                    // 현재 시간 기준 -6시간 ~ +6시간 사이인 데이터만 포함
                    return forecastTime.isAfter(now.minusHours(7)) &&
                            forecastTime.isBefore(now.plusHours(7));
                })
                .collect(Collectors.toList());
    }
	

    // 기상관측 조회 (캐시 우선)
    public List<ObsDto> getObsList() {
        ObsDto[] cached = fileCacheService.load(CACHE_OBS, ObsDto[].class);
        if (cached != null) {
            return Arrays.asList(cached);
        }
        return refreshObsList();
    }

    // 기상관측 API 호출 및 캐시 갱신
    public List<ObsDto> refreshObsList() {
        List<ObsDto> data = fetchObsList();
        if (!data.isEmpty()) {
            fileCacheService.save(CACHE_OBS, data);
        }
        return data;
    }

    // 기상관측 기상청 HUB API 데이터 조회
    private List<ObsDto> fetchObsList() {
        StringBuilder response = new StringBuilder();

        try {
            // 현재 시간 기준 -1시간 ~ +12시간 범위 설정
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
            
            String tm1 = now.minusHours(1).format(formatter);
            String tm2 = now.plusHours(12).format(formatter); 

            // 1. API 호출 설정
            String urlStr = "https://apihub.kma.go.kr/api/typ01/url/kma_sfctm3.php?tm1=" + tm1 + "&tm2=" + tm2 + "&stn=108&help=0&authKey=" + kmaAuthKey;
            URL url = new URL(urlStr);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json");
            
            // 2. 응답 읽기 (줄바꿈 포함)
            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "EUC-KR"))) { 
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine).append("\n"); // 줄바꿈을 꼭 넣어줘야 나중에 분리가 가능합니다.
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>(); // 에러 시 빈 리스트 반환
        }

        // 3. 파싱 로직 호출
        return parseTextToObsList(response.toString());
    }

    // Helper 텍스트 -> ObsDto 파싱
    private List<ObsDto> parseTextToObsList(String rawData) {
        return rawData.lines()
        .filter(line -> !line.startsWith("#")) // 주석 라인 제거
        .filter(line -> line.trim().length() > 50) // 알맹이 없는 짧은 줄 제거
        .map(line -> {
            String[] t = line.trim().split("\\s+"); // 여러 공백을 하나로 처리하여 분리
            
            // 빌더를 사용하여 DTO에 값 담기 (인덱스 순서 주의)
            return ObsDto.builder()
            .tm(t[0])
            .stn(parseInt(t[1]))
            .wd(parseInt(t[2]))
            .ws(parseDouble(t[3]))
            .pa(parseDouble(t[7]))
            .ps(parseDouble(t[8]))
            .ta(parseDouble(t[11])) // 12번째: 기온
            .td(parseDouble(t[12])) // 13번째: 이슬점
            .hm(parseDouble(t[13])) // 14번째: 습도
            .vs(parseInt(t[32])) // 33번째: 시정
            .ts(parseDouble(t[36])) // 37번째: 지면온도
            .build();
        })
        .collect(Collectors.toList());
    }
    // --- 결측치(-9, -9.0) 및 빈값 처리를 위한 헬퍼 메소드 ---
    private Double parseDouble(String s) {
        if (s == null || s.equals("-9") || s.equals("-9.0") || s.equals("-"))
            return null;
        try {
            return Double.parseDouble(s);
        } catch (Exception e) {
            return null;
        }
    }

    private Integer parseInt(String s) {
        if (s == null || s.equals("-9") || s.equals("-")) return null;
        try { return Integer.parseInt(s); } catch (Exception e) { return null; }
    }
    
    // 미세먼지 조회 (캐시 우선)
    public List<DustDto> getDustList() {
        DustDto[] cached = fileCacheService.load(CACHE_DUST, DustDto[].class);
        if (cached != null) {
            return Arrays.asList(cached);
        }
        return refreshDustList();
    }

    // 미세먼지 API 호출 및 캐시 갱신
    public List<DustDto> refreshDustList() {
        List<DustDto> data = fetchDustList();
        if (!data.isEmpty()) {
            fileCacheService.save(CACHE_DUST, data);
        }
        return data;
    }

    // 미세먼지 기상청 HUB API 데이터 조회
    private List<DustDto> fetchDustList() {
        StringBuilder response = new StringBuilder();
        
        try {
            // 현재 시간 기준 -1시간 ~ +12시간 범위 설정
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
            
            String tm1 = now.minusHours(1).format(formatter);
            String tm2 = now.plusHours(12).format(formatter); 

            // 1. API 호출 설정
            String urlStr = "https://apihub.kma.go.kr/api/typ01/url/kma_pm10.php?tm1=" + tm1 + "&tm2=" + tm2 + "&stn=108&authKey=" + kmaAuthKey;
            URL url = new URL(urlStr);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json");
            
            // 2. 응답 읽기 (줄바꿈 포함)
            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "EUC-KR"))) { 
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine).append("\n"); // 줄바꿈을 꼭 넣어줘야 나중에 분리가 가능합니다.
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>(); // 에러 시 빈 리스트 반환
        }
        // 3. 파싱 로직 호출
        return parseTextToDustList(response.toString());
    }

    // Helper 텍스트 -> DustDto 파싱
    private List<DustDto> parseTextToDustList(String rawData) {
        return rawData.lines()
                .filter(line -> !line.startsWith("#")) // 주석 라인 제거
                .filter(line -> line.trim().length() > 20) // 알맹이 없는 짧은 줄 제거 (길이 완화)
                .map(line -> {
                    // 콤마로 분리
                    String[] t = line.split(",", -1); 

                    // 빌더를 사용하여 DTO에 값 담기 (인덱스 순서 주의)
                    return DustDto.builder()
                            .tm(t[0].trim())
                            .stnId(parseInt(t[1].trim()))
                            .pm10(parseInt(t[2].trim()))
                            .flag(t[3].trim())
                            .mqc(t.length > 5 ? t[5].replace("=", "").trim() : "") // 마지막 = 제거
                            .build();
                })
                .collect(Collectors.toList());
    }

    // 자외선 조회 (캐시 우선)
    public List<UvDto> getUvList() {
        UvDto[] cached = fileCacheService.load(CACHE_UV, UvDto[].class);
        if (cached != null) {
            return Arrays.asList(cached);
        }
        return refreshUvList();
    }

    // 자외선 API 호출 및 캐시 갱신
    public List<UvDto> refreshUvList() {
        List<UvDto> data = fetchUvList();
        if (!data.isEmpty()) {
            fileCacheService.save(CACHE_UV, data);
        }
        return data;
    }

    // 자외선 기상청 HUB API 데이터 조회
    private List<UvDto> fetchUvList() {
        StringBuilder response = new StringBuilder();
        
        try {
            // 오늘 날짜 계산
            LocalDate today = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            String tm = today.format(formatter); 

            // 1. API 호출 설정
            String urlStr = "https://apihub.kma.go.kr/api/typ01/url/kma_sfctm_uv.php?tm=" + tm + "&stn=108&help=1&authKey=" + kmaAuthKey;
            URL url = new URL(urlStr);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json");
            
            // 2. 응답 읽기 (줄바꿈 포함)
            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "EUC-KR"))) { 
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine).append("\n"); // 줄바꿈을 꼭 넣어줘야 나중에 분리가 가능합니다.
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>(); // 에러 시 빈 리스트 반환
        }
        // 3. 파싱 로직 호출
        return parseTextToUvList(response.toString());
    }

    // Helper 텍스트 -> UvDto 파싱
    private List<UvDto> parseTextToUvList(String rawData) {
        return rawData.lines()
                .filter(line -> !line.startsWith("#")) // 주석 라인 제거
                .filter(line -> !line.startsWith("|")) // 헤더 라인 제거
                .filter(line -> line.trim().length() > 20) // 알맹이 없는 짧은 줄 제거 (길이 완화)
                .map(line -> {
                    // 공백으로 분리
                    String[] t = line.trim().split("\\s+");

                    // 빌더를 사용하여 DTO에 값 담기 (인덱스 순서 주의)
                    return UvDto.builder()
                            .tm(t[0])
                            .stn(parseInt(t[1]))
                            .uvb(parseDouble(t[2]))
                            .uva(parseDouble(t[3]))
                            .euv(parseDouble(t[4]))
                            .uvBIndex(parseDouble(t[5]))
                            .uvAIndex(parseDouble(t[6]))
                            .temp1(parseDouble(t[7]))
                            .temp2(parseDouble(t[8]))
                            .build();
                })
                .collect(Collectors.toList());
    }

    // 테스트 데이터 통합 조회
    public Map<String, Object> getCheckWeather() {
        Map<String, Object> weatherData = new HashMap<>();
        
        // 1. 단기 예보 (Forecast)
        //weatherData.put("forecast", getForecastList());
        
        // 2. 종관 관측 (Observation) - 현재 날씨
        List<ObsDto> obsList = getObsList();
        if (!obsList.isEmpty()) {
            weatherData.put("current", obsList.get(obsList.size() - 1)); // 가장 최근 데이터
        }
        
        // 3. 미세먼지 (Dust)
        List<DustDto> dustList = getDustList();
        if (!dustList.isEmpty()) { 
             // 보통 최근 데이터가 가장 뒤에 있거나 함. API 특성 확인 필요하지만 일단 0번째나 마지막 사용
             weatherData.put("dust", dustList.get(0)); 
        }

        // 4. 자외선 (UV)
        List<UvDto> uvList = getUvList();
        if (!uvList.isEmpty()) {
            weatherData.put("uv", uvList.get(0));
        }

        return weatherData;
    }

    // Helper 예보 기준 날짜 계산
    private String getBaseDate(LocalDateTime now) {
        // 02:10 이전이면 어제 날짜 사용
        if (now.getHour() < 2 || (now.getHour() == 2 && now.getMinute() < 10)) {
            return now.minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        }
        return now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    // Helper 예보 기준 시간 계산
    private String getBaseTime(LocalDateTime now) {
        // Base Time: 02, 05, 08, 11, 14, 17, 20, 23 (3시간 간격)
        // API 제공 시간: Base Time + 10분 뒤 (02:10, 05:10...)
        
        int hour = now.getHour();
        int minute = now.getMinute();

        // 02:10 이전이면 전날 23:00
        if (hour < 2 || (hour == 2 && minute < 10)) {
            return "2300";
        }

        // 그 외 시간: (시간 - 2) / 3 * 3 + 2 공식 사용 (가장 가까운 과거 Base Time)
        // 예: 04시 -> (2/3)*3 + 2 = 2 -> 0200
        // 예: 05시 15분 -> (3/3)*3 + 2 = 5 -> 0500
        
        // 단, 해당 시간의 10분 전이라면 이전 Base Time을 써야 함.
        // 예: 05:05 -> 아직 05시 데이터 안 나옴 -> 02시 데이터 써야 함.
        // 따라서 (현재시간 - 10분)을 기준으로 계산하면 편함.
        
        LocalDateTime adjustedTime = now.minusMinutes(10);
        int adjHour = adjustedTime.getHour();
        
        int baseHour = ((adjHour - 2) / 3) * 3 + 2;
        if (baseHour < 2) baseHour = 23; // 02시 이전 케이스는 위에서 걸러지긴 함

        return String.format("%02d00", baseHour);
    }

    // 스케줄러 전체 데이터 강제 갱신
    public void refreshAllWeatherData() {
        refreshForecastList();
        refreshObsList();
        refreshDustList();
        refreshUvList();
    }

}
