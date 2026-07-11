package com.neoulteo.ai.resource;

public record ToolExecutionResource(
        String toolName,
        Object input,
        Object result,
        boolean success,
        String message,
        long elapsedMillis) {
}
