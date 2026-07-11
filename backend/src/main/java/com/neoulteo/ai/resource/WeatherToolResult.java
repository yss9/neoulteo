package com.neoulteo.ai.resource;

public record WeatherToolResult(
        String region,
        String condition,
        String temperature,
        boolean precipitation,
        String message) {
}
