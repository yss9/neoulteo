package com.neoulteo.ai.resource;

import java.util.List;

public record TravelAssistantData(
        String answer,
        List<RagSourceResource> ragSources,
        List<ToolExecutionResource> toolResults) {
}
