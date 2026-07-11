package com.neoulteo.ai.resource;

public record TravelPlanEvaluationPlace(
        Integer dayNo,
        String name,
        String address,
        String contentTypeName,
        String contentTypeId,
        Double latitude,
        Double longitude) {
}
