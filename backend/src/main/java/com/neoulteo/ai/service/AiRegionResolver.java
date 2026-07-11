package com.neoulteo.ai.service;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class AiRegionResolver {
    private final Map<String, String> areaCodes = new LinkedHashMap<>();

    public AiRegionResolver() {
        areaCodes.put("서울", "1");
        areaCodes.put("인천", "2");
        areaCodes.put("대전", "3");
        areaCodes.put("대구", "4");
        areaCodes.put("광주", "5");
        areaCodes.put("부산", "6");
        areaCodes.put("울산", "7");
        areaCodes.put("세종", "8");
        areaCodes.put("경기", "31");
        areaCodes.put("강원", "32");
        areaCodes.put("충북", "33");
        areaCodes.put("충청북도", "33");
        areaCodes.put("충남", "34");
        areaCodes.put("충청남도", "34");
        areaCodes.put("경북", "35");
        areaCodes.put("경상북도", "35");
        areaCodes.put("경남", "36");
        areaCodes.put("경상남도", "36");
        areaCodes.put("전북", "37");
        areaCodes.put("전라북도", "37");
        areaCodes.put("전남", "38");
        areaCodes.put("전라남도", "38");
        areaCodes.put("제주", "39");
        areaCodes.put("제주도", "39");
    }

    public String resolveAreaCode(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        return areaCodes.entrySet().stream()
                .filter(entry -> text.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    public String resolveRegionName(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        return areaCodes.keySet().stream()
                .filter(text::contains)
                .findFirst()
                .orElse(null);
    }
}
