package com.kh.spring.weather.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ForecastDto {
    @JsonProperty("baseDate")
    private String baseDate;  // 발표일자
    @JsonProperty("baseTime")
    private String baseTime;  // 발표시각
    @JsonProperty("category")
    private String category;  // 자료구분 코드
    @JsonProperty("fcstDate")
    private String fcstDate;  // 예측일자
    @JsonProperty("fcstTime")
    private String fcstTime;  // 예측시각
    @JsonProperty("fcstValue")
    private String fcstValue; // 예보 값
    @JsonProperty("nx")
    private int nx;           // 예보지점 X 좌표
    @JsonProperty("ny")
    private int ny;           // 예보지점 Y 좌표
}
