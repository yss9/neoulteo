package com.neoulteo.ai.controller;

import com.neoulteo.ai.resource.AiChatRequest;
import com.neoulteo.ai.resource.AiChatResponse;
import com.neoulteo.ai.resource.TravelAssistantRequest;
import com.neoulteo.ai.resource.TravelAssistantResponse;
import com.neoulteo.ai.resource.TravelPlanEvaluationRequest;
import com.neoulteo.ai.service.AiTravelChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/app/ai")
public class AiTravelController {
    private final AiTravelChatService aiTravelChatService;

    public AiTravelController(AiTravelChatService aiTravelChatService) {
        this.aiTravelChatService = aiTravelChatService;
    }

    @PostMapping("/chat")
    public ResponseEntity<AiChatResponse> chat(@RequestBody AiChatRequest request) {
        return ResponseEntity.ok(aiTravelChatService.chat(normalizeMessage(request)));
    }

    @PostMapping("/smart-travel-chat")
    public ResponseEntity<AiChatResponse> smartTravelChat(@RequestBody AiChatRequest request) {
        return ResponseEntity.ok(aiTravelChatService.smartTravelChat(normalizeMessage(request)));
    }

    @PostMapping("/evaluate-plan")
    public ResponseEntity<AiChatResponse> evaluatePlan(@RequestBody TravelPlanEvaluationRequest request) {
        return ResponseEntity.ok(aiTravelChatService.evaluateTravelPlan(request));
    }

    @PostMapping("/travel-assistant")
    public ResponseEntity<TravelAssistantResponse> travelAssistant(@RequestBody TravelAssistantRequest request) {
        return ResponseEntity.ok(aiTravelChatService.travelAssistant(request));
    }

    private String normalizeMessage(AiChatRequest request) {
        if (request == null || request.message() == null || request.message().isBlank()) {
            return "관광지를 추천해줘";
        }
        return request.message().trim();
    }
}
