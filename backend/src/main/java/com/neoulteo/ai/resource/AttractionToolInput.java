package com.neoulteo.ai.resource;

public record AttractionToolInput(
        String region,
        String keyword,
        String contentTypeId,
        Integer limit) {
}
