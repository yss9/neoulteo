package com.neoulteo.ai.resource;

public record AiChatResponse(boolean success, String message, AiChatData data) {
    public static AiChatResponse success(String answer, java.util.List<ToolExecutionResource> toolResults) {
        return new AiChatResponse(true, "응답 생성 성공", new AiChatData(answer, toolResults));
    }
}
