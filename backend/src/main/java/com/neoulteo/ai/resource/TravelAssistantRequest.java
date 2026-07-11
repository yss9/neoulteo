package com.neoulteo.ai.resource;

public record TravelAssistantRequest(
        String message,
        Boolean useRag,
        Boolean useTools) {
}
