package com.neoulteo.ai.tool;

import java.util.List;

import com.neoulteo.ai.resource.TravelSearchToolInput;
import com.neoulteo.ai.resource.TravelSearchToolResult;
import com.neoulteo.ai.service.TravelSearchToolService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class TravelSearchTool {
    private static final Logger log = LoggerFactory.getLogger(TravelSearchTool.class);
    private final TravelSearchToolService travelSearchToolService;

    public TravelSearchTool(TravelSearchToolService travelSearchToolService) {
        this.travelSearchToolService = travelSearchToolService;
    }

    @Tool(description = "축제, 행사, 최신 여행 정보를 조회한다.")
    public List<TravelSearchToolResult> searchTravelInfo(TravelSearchToolInput input) {
        long startedAt = System.currentTimeMillis();
        List<TravelSearchToolResult> result = travelSearchToolService.search(input);
        log.info("AI tool executed name=TravelSearchTool input={} elapsedMs={} success=true",
                input == null ? "{}" : "{region=%s, keyword=%s, limit=%s}".formatted(input.region(), input.keyword(), input.limit()),
                System.currentTimeMillis() - startedAt);
        return result;
    }
}
