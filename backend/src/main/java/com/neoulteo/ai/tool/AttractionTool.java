package com.neoulteo.ai.tool;

import java.util.List;

import com.neoulteo.ai.resource.AttractionToolInput;
import com.neoulteo.ai.resource.AttractionToolResult;
import com.neoulteo.ai.service.AttractionToolService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class AttractionTool {
    private static final Logger log = LoggerFactory.getLogger(AttractionTool.class);
    private final AttractionToolService attractionToolService;

    public AttractionTool(AttractionToolService attractionToolService) {
        this.attractionToolService = attractionToolService;
    }

    @Tool(description = "기존 DB에 저장된 관광지를 지역, 키워드, 유형 기준으로 조회한다.")
    public List<AttractionToolResult> searchAttractions(AttractionToolInput input) {
        long startedAt = System.currentTimeMillis();
        try {
            List<AttractionToolResult> result = attractionToolService.search(input);
            log.info("AI tool executed name=AttractionTool input={} elapsedMs={} success=true",
                    safeInput(input), System.currentTimeMillis() - startedAt);
            return result;
        } catch (RuntimeException e) {
            log.warn("AI tool failed name=AttractionTool input={} elapsedMs={} error={}",
                    safeInput(input), System.currentTimeMillis() - startedAt, e.getMessage());
            throw e;
        }
    }

    private String safeInput(AttractionToolInput input) {
        if (input == null) {
            return "{}";
        }
        return "{region=%s, keyword=%s, contentTypeId=%s, limit=%s}"
                .formatted(input.region(), input.keyword(), input.contentTypeId(), input.limit());
    }
}
