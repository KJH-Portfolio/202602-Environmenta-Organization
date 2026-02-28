package com.kh.spring.weather.model.vo;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonDeserialize(using = ForecastResultDeserializer.class)
public class ForecastResult {
    private List<ForecastDto> forecastList;
}
