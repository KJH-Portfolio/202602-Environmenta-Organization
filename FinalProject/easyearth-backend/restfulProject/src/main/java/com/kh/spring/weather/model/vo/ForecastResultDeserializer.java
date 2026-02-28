package com.kh.spring.weather.model.vo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ForecastResultDeserializer extends JsonDeserializer<ForecastResult> {

    @Override
    public ForecastResult deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode rootNode = p.getCodec().readTree(p);
        // response -> body -> items -> item 경로 탐색
        JsonNode itemsNode = rootNode.path("response").path("body").path("items").path("item");

        List<ForecastDto> list = new ArrayList<>();
        if (itemsNode.isArray()) {
            ObjectMapper mapper = (ObjectMapper) p.getCodec();
            for (JsonNode itemNode : itemsNode) {
                // itemNode를 ForecastDto로 변환 (이미 @JsonProperty가 붙어있으므로 매핑됨)
                ForecastDto dto = mapper.treeToValue(itemNode, ForecastDto.class);
                list.add(dto);
            }
        }
        return new ForecastResult(list);
    }
}
