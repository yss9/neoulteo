package com.neoulteo.ai.tool;

import com.neoulteo.ai.resource.WeatherToolInput;
import com.neoulteo.ai.resource.WeatherToolResult;
import com.neoulteo.ai.service.WeatherToolService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class WeatherTool {
    private static final Logger log = LoggerFactory.getLogger(WeatherTool.class);
    private final WeatherToolService weatherToolService;

    public WeatherTool(WeatherToolService weatherToolService) {
        this.weatherToolService = weatherToolService;
    }

    @Tool(description = "여행지 또는 지역 기준 날씨 상태를 조회한다.")
    public WeatherToolResult getWeather(WeatherToolInput input) {
        long startedAt = System.currentTimeMillis();
        WeatherToolResult result = weatherToolService.getWeather(input);
        log.info("AI tool executed name=WeatherTool input={} elapsedMs={} success=true",
                input == null ? "{}" : "{region=%s}".formatted(input.region()),
                System.currentTimeMillis() - startedAt);
        return result;
    }
}
