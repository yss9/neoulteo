package com.neoulteo.ai.service;

import java.util.List;
import java.util.Map;

import com.neoulteo.ai.resource.AttractionToolInput;
import com.neoulteo.ai.resource.AttractionToolResult;
import com.neoulteo.domain.attraction.dao.DbAttractionDao;
import org.springframework.stereotype.Service;

@Service
public class AttractionToolService {
    private final DbAttractionDao attractionDao;
    private final AiRegionResolver regionResolver;

    public AttractionToolService(DbAttractionDao attractionDao, AiRegionResolver regionResolver) {
        this.attractionDao = attractionDao;
        this.regionResolver = regionResolver;
    }

    public List<AttractionToolResult> search(AttractionToolInput input) {
        int limit = normalizeLimit(input == null ? null : input.limit());
        String queryText = buildQueryText(input);
        String areaCode = regionResolver.resolveAreaCode(queryText);
        String contentTypeId = input == null ? null : input.contentTypeId();
        String keyword = normalizeKeyword(input == null ? null : input.keyword());

        List<Map<String, String>> rows = attractionDao.searchAttractions(areaCode, null, contentTypeId, keyword);
        if (rows.isEmpty() && keyword != null) {
            rows = attractionDao.searchAttractions(areaCode, null, contentTypeId, null);
        }

        return rows.stream()
                .limit(limit)
                .map(this::toResult)
                .toList();
    }

    private AttractionToolResult toResult(Map<String, String> row) {
        return new AttractionToolResult(
                value(row, "title"),
                value(row, "addr1"),
                value(row, "contentTypeName"),
                shortText(value(row, "overview")),
                value(row, "mapy"),
                value(row, "mapx"));
    }

    private String buildQueryText(AttractionToolInput input) {
        if (input == null) {
            return "";
        }
        return String.join(" ",
                input.region() == null ? "" : input.region(),
                input.keyword() == null ? "" : input.keyword());
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        String normalized = keyword
                .replace("추천", "")
                .replace("알려줘", "")
                .replace("관광지", "")
                .replace("근처", "")
                .trim();
        if (normalized.contains("실내")) {
            return "실내";
        }
        if (normalized.contains("바다")) {
            return "바다";
        }
        return normalized.isBlank() ? null : normalized;
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null) {
            return 5;
        }
        return Math.max(1, Math.min(limit, 20));
    }

    private String value(Map<String, String> row, String key) {
        Object value = row.get(key);
        return value == null ? "" : String.valueOf(value);
    }

    private String shortText(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.length() > 120 ? value.substring(0, 120) + "..." : value;
    }
}
