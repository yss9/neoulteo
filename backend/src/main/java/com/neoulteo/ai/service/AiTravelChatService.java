package com.neoulteo.ai.service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.neoulteo.ai.config.NeoulteoAiProperties;
import com.neoulteo.ai.prompt.TravelAiPromptProvider;
import com.neoulteo.ai.resource.AiChatResponse;
import com.neoulteo.ai.resource.AttractionToolInput;
import com.neoulteo.ai.resource.RagSourceResource;
import com.neoulteo.ai.resource.ToolExecutionResource;
import com.neoulteo.ai.resource.TravelAssistantData;
import com.neoulteo.ai.resource.TravelAssistantRequest;
import com.neoulteo.ai.resource.TravelAssistantResponse;
import com.neoulteo.ai.resource.TravelPlanEvaluationPlace;
import com.neoulteo.ai.resource.TravelPlanEvaluationRequest;
import com.neoulteo.ai.resource.TravelSearchToolInput;
import com.neoulteo.ai.resource.WeatherToolInput;
import com.neoulteo.ai.tool.AttractionTool;
import com.neoulteo.ai.tool.TravelSearchTool;
import com.neoulteo.ai.tool.WeatherTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

@Service
public class AiTravelChatService {
    private final AttractionTool attractionTool;
    private final WeatherTool weatherTool;
    private final TravelSearchTool travelSearchTool;
    private final AiRegionResolver regionResolver;
    private final RagSearchService ragSearchService;
    private final TravelAiPromptProvider promptProvider;
    private final NeoulteoAiProperties properties;
    private final ObjectProvider<ChatClient.Builder> chatClientBuilderProvider;

    public AiTravelChatService(
            AttractionTool attractionTool,
            WeatherTool weatherTool,
            TravelSearchTool travelSearchTool,
            AiRegionResolver regionResolver,
            RagSearchService ragSearchService,
            TravelAiPromptProvider promptProvider,
            NeoulteoAiProperties properties,
            ObjectProvider<ChatClient.Builder> chatClientBuilderProvider) {
        this.attractionTool = attractionTool;
        this.weatherTool = weatherTool;
        this.travelSearchTool = travelSearchTool;
        this.regionResolver = regionResolver;
        this.ragSearchService = ragSearchService;
        this.promptProvider = promptProvider;
        this.properties = properties;
        this.chatClientBuilderProvider = chatClientBuilderProvider;
    }

    public AiChatResponse chat(String message) {
        List<ToolExecutionResource> toolResults = new ArrayList<>();
        AttractionToolInput input = new AttractionToolInput(
                resolveRegion(message),
                message,
                resolveContentType(message),
                resolveLimit(message));
        toolResults.add(execute("AttractionTool", input, () -> attractionTool.searchAttractions(input)));
        String answer = generateAnswer(message, toolResults);
        return AiChatResponse.success(answer, toolResults);
    }

    public AiChatResponse smartTravelChat(String message) {
        List<ToolExecutionResource> toolResults = executeTravelTools(message);
        String answer = generateAnswer(message, toolResults);
        return AiChatResponse.success(answer, toolResults);
    }

    public AiChatResponse evaluateTravelPlan(TravelPlanEvaluationRequest request) {
        String answer = generatePlanEvaluationAnswer(request);
        return AiChatResponse.success(answer, List.of());
    }

    public TravelAssistantResponse travelAssistant(TravelAssistantRequest request) {
        String message = request == null || request.message() == null || request.message().isBlank()
                ? "여행지를 추천해줘"
                : request.message().trim();
        boolean useRag = request == null || request.useRag() == null || request.useRag();
        boolean useTools = request == null || request.useTools() == null || request.useTools();

        List<RagSourceResource> ragSources = useRag ? ragSearchService.search(message) : List.of();
        List<ToolExecutionResource> toolResults = useTools ? executeTravelTools(message) : List.of();
        String answer = generateRagToolAnswer(message, ragSources, toolResults);
        return TravelAssistantResponse.success(new TravelAssistantData(answer, ragSources, toolResults));
    }

    private List<ToolExecutionResource> executeTravelTools(String message) {
        List<ToolExecutionResource> toolResults = new ArrayList<>();
        String region = resolveRegion(message);

        if (containsAny(message, "날씨", "비", "눈", "더워", "추워")) {
            WeatherToolInput weatherInput = new WeatherToolInput(region);
            toolResults.add(execute("WeatherTool", weatherInput, () -> weatherTool.getWeather(weatherInput)));
        }

        if (containsAny(message, "축제", "행사", "이벤트", "최신")) {
            TravelSearchToolInput searchInput =
                    new TravelSearchToolInput(region, resolveTravelKeyword(message), resolveLimit(message));
            toolResults.add(execute("TravelSearchTool", searchInput,
                    () -> travelSearchTool.searchTravelInfo(searchInput)));
        }

        AttractionToolInput attractionInput = new AttractionToolInput(
                region,
                resolveAttractionKeyword(message),
                resolveContentType(message),
                resolveLimit(message));
        toolResults.add(execute("AttractionTool", attractionInput, () -> attractionTool.searchAttractions(attractionInput)));
        return toolResults;
    }

    private ToolExecutionResource execute(String toolName, Object input, Supplier<Object> supplier) {
        long startedAt = System.currentTimeMillis();
        try {
            Object result = supplier.get();
            return new ToolExecutionResource(toolName, input, result, true, "Tool execution succeeded",
                    System.currentTimeMillis() - startedAt);
        } catch (RuntimeException e) {
            return new ToolExecutionResource(toolName, input, null, false, e.getMessage(),
                    System.currentTimeMillis() - startedAt);
        }
    }

    private String generateAnswer(String message, List<ToolExecutionResource> toolResults) {
        ChatClient.Builder builder = chatClientBuilderProvider.getIfAvailable();
        if (!properties.getSpringAi().isEnabled() || builder == null) {
            return promptProvider.fallbackAnswer(message, toolResults);
        }

        try {
            return builder.build()
                    .prompt()
                    .system(promptProvider.systemPrompt())
                    .user(promptProvider.userPrompt(message, toolResults))
                    .call()
                    .content();
        } catch (RuntimeException e) {
            return promptProvider.fallbackAnswer(message, toolResults)
                    + "\n\nLLM 응답 생성 실패: " + e.getMessage();
        }
    }

    private String generatePlanEvaluationAnswer(TravelPlanEvaluationRequest request) {
        ChatClient.Builder builder = chatClientBuilderProvider.getIfAvailable();
        String prompt = buildPlanEvaluationPrompt(request);

        if (!properties.getSpringAi().isEnabled() || builder == null) {
            return fallbackPlanEvaluationAnswer(request);
        }

        try {
            return builder.build()
                    .prompt()
                    .system("""
                            너는 Neoulteo 여행계획 평가 AI다.
                            사용자가 만든 여행 코스를 일정 강도, 이동 동선, 카테고리 균형 기준으로 구체적으로 평가한다.
                            제공된 장소, 일차, 좌표, 카테고리 정보만 근거로 사용하고 확인되지 않은 내용은 지어내지 않는다.
                            답변은 5~8문장으로 작성하고, 마지막에는 바로 실행할 수 있는 수정 제안 2개를 짧게 제시한다.
                            """)
                    .user(prompt + planEvaluationJsonInstruction())
                    .call()
                    .content();
        } catch (RuntimeException e) {
            return fallbackPlanEvaluationAnswer(request) + "\n\nLLM 코스 평가 실패: " + e.getMessage();
        }
    }

    private String generateRagToolAnswer(String message, List<RagSourceResource> ragSources,
            List<ToolExecutionResource> toolResults) {
        ChatClient.Builder builder = chatClientBuilderProvider.getIfAvailable();
        String prompt = buildRagToolPrompt(message, ragSources, toolResults);

        if (!properties.getSpringAi().isEnabled() || builder == null) {
            return fallbackRagToolAnswer(ragSources, toolResults);
        }

        try {
            return builder.build()
                    .prompt()
                    .system("""
                            너는 Neoulteo의 여행 어시스턴트다.
                            답변은 제공된 RAG 참고 문서와 Tool 실행 결과를 우선 근거로 사용한다.
                            근거가 없는 정보는 지어내지 말고, 확인하지 못했다고 말한다.
                            사용자가 바로 여행 계획에 활용할 수 있도록 한국어로 구체적이고 간결하게 답한다.
                            """)
                    .user(prompt)
                    .call()
                    .content();
        } catch (RuntimeException e) {
            return fallbackRagToolAnswer(ragSources, toolResults) + "\n\nLLM 응답 생성 실패: " + e.getMessage();
        }
    }

    private String buildRagToolPrompt(String message, List<RagSourceResource> ragSources,
            List<ToolExecutionResource> toolResults) {
        StringBuilder sb = new StringBuilder();
        sb.append("사용자 질문:\n").append(message).append("\n\n");
        sb.append("[RAG 참고 문서]\n");
        if (ragSources == null || ragSources.isEmpty()) {
            sb.append("- 검색된 문서 없음\n");
        } else {
            for (int i = 0; i < ragSources.size(); i++) {
                RagSourceResource source = ragSources.get(i);
                sb.append(i + 1)
                        .append(". ")
                        .append(source.title())
                        .append(" / score=")
                        .append(String.format(java.util.Locale.ROOT, "%.3f", source.score()))
                        .append('\n')
                        .append(source.content())
                        .append("\n\n");
            }
        }

        sb.append("[Tool 실행 결과]\n");
        if (toolResults == null || toolResults.isEmpty()) {
            sb.append("- 실행된 Tool 없음\n");
        } else {
            for (ToolExecutionResource toolResult : toolResults) {
                sb.append("- ")
                        .append(toolResult.toolName())
                        .append(" success=")
                        .append(toolResult.success())
                        .append(" message=")
                        .append(toolResult.message())
                        .append('\n')
                        .append("  result=")
                        .append(toolResult.result())
                        .append('\n');
            }
        }

        sb.append("""

                위 근거를 조합해 답변하세요.
                - 관련 관광지나 코스가 있으면 3~5개 중심으로 추천
                - RAG 문서와 Tool 결과가 다르면 Tool 결과를 우선
                - 날씨나 최신 정보 Tool이 미연동이면 한계를 짧게 안내
                """);
        return sb.toString();
    }

    private String fallbackRagToolAnswer(List<RagSourceResource> ragSources, List<ToolExecutionResource> toolResults) {
        StringBuilder sb = new StringBuilder("Spring AI LLM 호출이 비활성화되어 검색 결과 기반 요약만 표시합니다.");
        if (ragSources != null && !ragSources.isEmpty()) {
            sb.append("\n\nRAG 참고 문서:");
            int limit = Math.min(ragSources.size(), 3);
            for (int i = 0; i < limit; i++) {
                RagSourceResource source = ragSources.get(i);
                sb.append("\n- ").append(source.title()).append(": ").append(source.content());
            }
        }
        if (toolResults != null && !toolResults.isEmpty()) {
            sb.append("\n\nTool 실행:");
            for (ToolExecutionResource toolResult : toolResults) {
                sb.append("\n- ").append(toolResult.toolName()).append(": ").append(toolResult.message());
            }
        }
        return sb.toString();
    }

    private String buildPlanEvaluationPrompt(TravelPlanEvaluationRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("여행 계획 제목: ").append(safe(request == null ? null : request.title())).append('\n');
        sb.append("여행 일수: ").append(request == null || request.durationDays() == null ? "-" : request.durationDays()).append("일\n");
        sb.append("규칙 기반 점수: ").append(request == null || request.score() == null ? "-" : request.score()).append("/100\n");
        sb.append("규칙 기반 요약: ").append(safe(request == null ? null : request.ruleSummary())).append('\n');
        sb.append("규칙 기반 피드백:\n");
        if (request != null && request.ruleFeedback() != null && !request.ruleFeedback().isEmpty()) {
            for (String item : request.ruleFeedback()) {
                sb.append("- ").append(safe(item)).append('\n');
            }
        } else {
            sb.append("- 없음\n");
        }

        sb.append("\n장소 목록:\n");
        if (request == null || request.places() == null || request.places().isEmpty()) {
            sb.append("- 장소 없음\n");
            return sb.toString();
        }

        int index = 1;
        for (TravelPlanEvaluationPlace place : request.places()) {
            sb.append(index++)
                    .append(". ")
                    .append(place.dayNo() == null ? 1 : place.dayNo())
                    .append("일차 / ")
                    .append(safe(place.name()))
                    .append(" / 주소: ")
                    .append(safe(place.address()))
                    .append(" / 카테고리: ")
                    .append(safe(place.contentTypeName() != null ? place.contentTypeName() : place.contentTypeId()))
                    .append(" / 좌표: ")
                    .append(place.latitude() == null ? "-" : place.latitude())
                    .append(", ")
                    .append(place.longitude() == null ? "-" : place.longitude())
                    .append('\n');
        }

        sb.append("""

                평가해주세요:
                - 하루 장소 수가 너무 많지 않은지
                - 이동 동선이 비효율적인지
                - 관광, 문화, 음식, 숙박 등 카테고리 균형이 어떤지
                - 어느 일차를 어떻게 바꾸면 좋은지
                """);
        return sb.toString();
    }

    private String planEvaluationJsonInstruction() {
        return """

                IMPORTANT OUTPUT FORMAT:
                Return only valid JSON. Do not use markdown fences or extra text.
                The JSON must use this schema:
                {
                  "score": 0,
                  "summary": "Korean one-line course evaluation summary",
                  "feedback": [
                    "Korean concrete feedback item",
                    "Korean concrete feedback item",
                    "Korean concrete feedback item"
                  ],
                  "advice": "Korean detailed route improvement advice. Mention what to keep and what to change."
                }
                Score must be an integer from 0 to 100.
                Score must be your own AI evaluation score. Do not simply copy the rule-based score.
                Use the rule-based score as one reference, then judge the itinerary yourself from place count,
                daily balance, route distance, category mix, addresses and coordinates.
                """;
    }

    private String fallbackPlanEvaluationAnswer(TravelPlanEvaluationRequest request) {
        if (request == null || request.places() == null || request.places().isEmpty()) {
            return "평가할 여행 코스가 없습니다. 장소를 먼저 추가한 뒤 다시 평가해보세요.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("규칙 기반 평가 결과, 이 코스는 ")
                .append(request.score() == null ? "-" : request.score())
                .append("점입니다. ");
        sb.append(safe(request.ruleSummary()));
        if (request.ruleFeedback() != null && !request.ruleFeedback().isEmpty()) {
            sb.append(" 주요 확인 사항은 ");
            int limit = Math.min(request.ruleFeedback().size(), 3);
            for (int i = 0; i < limit; i++) {
                if (i > 0) {
                    sb.append(" / ");
                }
                sb.append(request.ruleFeedback().get(i));
            }
            sb.append("입니다.");
        }
        sb.append("\n\nSpring AI LLM 호출이 비활성화되어 규칙 기반 평가만 표시합니다.");
        return sb.toString();
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String resolveRegion(String message) {
        return regionResolver.resolveRegionName(message);
    }

    private String resolveContentType(String message) {
        if (containsAny(message, "실내", "문화", "박물관", "전시")) {
            return "14";
        }
        if (containsAny(message, "축제", "공연", "행사")) {
            return "15";
        }
        if (containsAny(message, "숙소", "숙박", "호텔")) {
            return "32";
        }
        if (containsAny(message, "맛집", "음식", "식당")) {
            return "39";
        }
        return null;
    }

    private String resolveAttractionKeyword(String message) {
        if (message == null) {
            return null;
        }
        if (containsAny(message, "비", "실내")) {
            return "실내";
        }
        if (containsAny(message, "바다", "해변", "해수욕장")) {
            return "바다";
        }
        if (containsAny(message, "아이", "가족")) {
            return "체험";
        }
        return message;
    }

    private String resolveTravelKeyword(String message) {
        if (containsAny(message, "축제")) {
            return "축제";
        }
        if (containsAny(message, "행사")) {
            return "행사";
        }
        return message;
    }

    private Integer resolveLimit(String message) {
        if (message == null) {
            return 5;
        }
        Matcher matcher = Pattern.compile("(\\d+)\\s*(개|곳)").matcher(message);
        if (matcher.find()) {
            return Math.max(1, Math.min(Integer.parseInt(matcher.group(1)), 20));
        }
        return 5;
    }

    private boolean containsAny(String text, String... values) {
        if (text == null) {
            return false;
        }
        for (String value : values) {
            if (text.contains(value)) {
                return true;
            }
        }
        return false;
    }
}
