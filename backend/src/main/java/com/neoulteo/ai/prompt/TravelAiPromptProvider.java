package com.neoulteo.ai.prompt;

import java.util.List;

import com.neoulteo.ai.resource.ToolExecutionResource;
import org.springframework.stereotype.Component;

@Component
public class TravelAiPromptProvider {
    public String systemPrompt() {
        return String.join("\n",
                "너는 관광/여행 추천 도우미다.",
                "관광지 정보는 반드시 AttractionTool 결과를 기반으로 답한다.",
                "날씨 정보는 반드시 WeatherTool 결과를 기반으로 답한다.",
                "축제/행사/최신 정보는 반드시 TravelSearchTool 결과를 기반으로 답한다.",
                "Tool 결과에 없는 정보는 만들어내지 않는다.",
                "정보가 부족하면 부족하다고 말한다.",
                "추천 이유는 짧고 구체적으로 작성한다.",
                "관광지명, 지역, 추천 이유를 포함한다.");
    }

    public String userPrompt(String message, List<ToolExecutionResource> toolResults) {
        return String.join("\n\n",
                "사용자 질문:\n" + safe(message),
                "Tool 실행 결과:\n" + formatToolResults(toolResults),
                "위 Tool 결과만 근거로 한국어로 답변해줘.");
    }

    public String fallbackAnswer(String message, List<ToolExecutionResource> toolResults) {
        StringBuilder sb = new StringBuilder();
        sb.append("요청: ").append(safe(message)).append("\n\n");
        for (ToolExecutionResource toolResult : toolResults) {
            sb.append("[").append(toolResult.toolName()).append("] ");
            if (!toolResult.success()) {
                sb.append(toolResult.message()).append("\n");
                continue;
            }
            sb.append(toolResult.result()).append("\n");
        }
        sb.append("\nSpring AI LLM 호출이 비활성화되어 Tool 결과 기반 요약만 표시합니다.");
        return sb.toString();
    }

    private String formatToolResults(List<ToolExecutionResource> toolResults) {
        if (toolResults == null || toolResults.isEmpty()) {
            return "- 실행된 Tool 없음";
        }

        StringBuilder sb = new StringBuilder();
        for (ToolExecutionResource toolResult : toolResults) {
            sb.append("- toolName: ").append(toolResult.toolName()).append('\n')
                    .append("  input: ").append(toolResult.input()).append('\n')
                    .append("  success: ").append(toolResult.success()).append('\n')
                    .append("  message: ").append(toolResult.message()).append('\n')
                    .append("  result: ").append(toolResult.result()).append('\n');
        }
        return sb.toString();
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "" : value;
    }
}
