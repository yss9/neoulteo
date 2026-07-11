package com.neoulteo.ai.resource;

public record TravelAssistantResponse(
        boolean success,
        String message,
        TravelAssistantData data) {

    public static TravelAssistantResponse success(TravelAssistantData data) {
        return new TravelAssistantResponse(true, "응답 생성 성공", data);
    }
}
