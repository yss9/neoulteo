package com.neoulteo.ai.resource;

import java.util.Map;

public record RagSourceResource(
        String title,
        String sourceType,
        String content,
        double score,
        Map<String, Object> metadata) {
}
