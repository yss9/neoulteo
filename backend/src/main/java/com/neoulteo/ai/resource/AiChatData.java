package com.neoulteo.ai.resource;

import java.util.List;

public record AiChatData(String answer, List<ToolExecutionResource> toolResults) {
}
