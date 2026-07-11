package com.neoulteo.ai.service;

import com.neoulteo.ai.config.NeoulteoAiProperties;
import com.neoulteo.ai.resource.WeatherToolInput;
import com.neoulteo.ai.resource.WeatherToolResult;
import org.springframework.stereotype.Service;

@Service
public class WeatherToolService {
    private final NeoulteoAiProperties properties;

    public WeatherToolService(NeoulteoAiProperties properties) {
        this.properties = properties;
    }

    public WeatherToolResult getWeather(WeatherToolInput input) {
        String region = input == null || input.region() == null || input.region().isBlank()
                ? "지역 미상"
                : input.region();
        if (properties.getWeather().getApiKey() == null || properties.getWeather().getApiKey().isBlank()) {
            return new WeatherToolResult(region, "확인 불가", "", false, "날씨 API 키가 설정되지 않았습니다.");
        }
        return new WeatherToolResult(region, "확장 필요", "", false, "날씨 API 연동 구조만 준비되어 있습니다.");
    }
}
