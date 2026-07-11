package com.neoulteo.ai.service;

import java.util.List;

import com.neoulteo.ai.config.NeoulteoAiProperties;
import com.neoulteo.ai.resource.AttractionToolInput;
import com.neoulteo.ai.resource.TravelSearchToolInput;
import com.neoulteo.ai.resource.TravelSearchToolResult;
import org.springframework.stereotype.Service;

@Service
public class TravelSearchToolService {
    private final NeoulteoAiProperties properties;
    private final AttractionToolService attractionToolService;

    public TravelSearchToolService(NeoulteoAiProperties properties, AttractionToolService attractionToolService) {
        this.properties = properties;
        this.attractionToolService = attractionToolService;
    }

    public List<TravelSearchToolResult> search(TravelSearchToolInput input) {
        String keyword = input == null || input.keyword() == null || input.keyword().isBlank()
                ? "축제"
                : input.keyword();
        List<TravelSearchToolResult> localFestivalResults = attractionToolService
                .search(new AttractionToolInput(input == null ? null : input.region(), keyword, "15",
                        input == null ? 5 : input.limit()))
                .stream()
                .map(item -> new TravelSearchToolResult(item.name(), item.address(), ""))
                .toList();

        if (!localFestivalResults.isEmpty()) {
            return localFestivalResults;
        }
        if (properties.getTravelSearch().getApiKey() == null || properties.getTravelSearch().getApiKey().isBlank()) {
            return List.of(new TravelSearchToolResult("검색 API 미설정", "TravelSearch API 키가 설정되지 않았습니다.", ""));
        }
        return List.of(new TravelSearchToolResult("검색 API 확장 필요", "외부 검색 API 연동 구조만 준비되어 있습니다.", ""));
    }
}
