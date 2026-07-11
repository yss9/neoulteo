package com.neoulteo.ai.resource;

import java.util.List;

public record TravelPlanEvaluationRequest(
        String title,
        Integer durationDays,
        Integer score,
        String ruleSummary,
        List<String> ruleFeedback,
        List<TravelPlanEvaluationPlace> places) {
}
