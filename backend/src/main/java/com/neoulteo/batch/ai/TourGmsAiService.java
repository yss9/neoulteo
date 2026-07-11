package com.neoulteo.batch.ai;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.neoulteo.batch.dto.TourAttractionChangeDto;
import com.neoulteo.batch.dto.TourAttractionDto;
import com.neoulteo.batch.dto.TourBatchComparisonResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class TourGmsAiService {
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${gms.openai.chat-url}")
    private String chatUrl;

    @Value("${gms.openai.api-key}")
    private String apiKey;

    @Value("${gms.openai.model:gpt-4o-mini}")
    private String model;

    public TourGmsAiService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(15));
        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    public String summarizeComparison(TourBatchComparisonResult comparison) {
        if (comparison == null) {
            return "TourAPI 비교 결과가 없습니다.";
        }
        if (apiKey == null || apiKey.isBlank()) {
            return fallbackComparisonSummary(comparison)
                    + "\n\nGMS API 키가 설정되지 않아 AI 요약을 건너뛰었습니다.";
        }

        String prompt = String.join("\n",
                "Write a Neoulteo TourAPI DB comparison briefing in Korean.",
                "Requirements:",
                "- Use concise Korean.",
                "- Explain new attractions, changed attractions, and missing candidates.",
                "- Do not invent facts outside the provided comparison data.",
                "- End with one practical maintenance suggestion.",
                "",
                "Counts:",
                "- API items: " + comparison.getApiItemCount(),
                "- New: " + comparison.getNewItemCount(),
                "- Changed: " + comparison.getChangedItemCount(),
                "- Missing candidates: " + comparison.getMissingItemCount(),
                "- Unchanged: " + comparison.getUnchangedItemCount(),
                "",
                "Content type summary:",
                buildContentTypeSummary(comparison),
                "",
                "New attractions:",
                buildChangeText(comparison.getNewItems()),
                "",
                "Changed attractions:",
                buildChangeText(comparison.getChangedItems()),
                "",
                "Missing candidates:",
                buildChangeText(comparison.getMissingItems()));

        Map<String, Object> payload = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "developer", "content",
                                "You write concise Korean data maintenance reports for a travel service."),
                        Map.of("role", "user", "content", prompt)));

        try {
            String response = restClient.post()
                    .uri(chatUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + apiKey)
                    .body(payload)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(response);
            String content = root.at("/choices/0/message/content").asText("");
            return content.isBlank() ? fallbackComparisonSummary(comparison) : content;
        } catch (Exception e) {
            return fallbackComparisonSummary(comparison) + "\n\nAI 요약 호출 실패: " + e.getMessage();
        }
    }

    public String summarizeAttractions(List<TourAttractionDto> items) {
        if (items == null || items.isEmpty()) {
            return "\uC218\uC9D1\uB41C \uAD00\uAD11\uC9C0 \uB370\uC774\uD130\uAC00 \uC5C6\uC2B5\uB2C8\uB2E4.";
        }
        if (apiKey == null || apiKey.isBlank()) {
            return "GMS API \uD0A4\uAC00 \uC124\uC815\uB418\uC9C0 \uC54A\uC544 AI \uC694\uC57D\uC744 \uAC74\uB108\uB6F0\uC5C8\uC2B5\uB2C8\uB2E4.";
        }

        String prompt = String.join("\n",
                "Write a Neoulteo daily tourism briefing in Korean.",
                "Requirements:",
                "- Use 5 to 7 Korean sentences.",
                "- Naturally mention three representative attractions.",
                "- Recommend where a traveler could visit today.",
                "- Include the areaCode and sigunguCode for recommended attractions when available.",
                "- Do not invent facts outside the provided data.",
                "",
                "Attractions:",
                buildAttractionText(items));

        Map<String, Object> payload = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "developer", "content",
                                "You write concise Korean travel briefing reports."),
                        Map.of("role", "user", "content", prompt)));

        try {
            String response = restClient.post()
                    .uri(chatUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + apiKey)
                    .body(payload)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(response);
            String content = root.at("/choices/0/message/content").asText("");
            return content.isBlank() ? fallbackSummary(items) : content;
        } catch (Exception e) {
            return fallbackSummary(items) + "\n\nAI \uC694\uC57D \uD638\uCD9C \uC2E4\uD328: " + e.getMessage();
        }
    }

    private String buildAttractionText(List<TourAttractionDto> items) {
        StringBuilder sb = new StringBuilder();
        int limit = Math.min(items.size(), 12);
        for (int i = 0; i < limit; i++) {
            TourAttractionDto item = items.get(i);
            sb.append(i + 1)
                    .append(". ")
                    .append(safe(item.getTitle()))
                    .append(" / \uC8FC\uC18C: ")
                    .append(safe(item.getAddr1()))
                    .append(" / \uC9C0\uC5ED\uCF54\uB4DC: ")
                    .append(safe(item.getAreaCode()))
                    .append(" / \uC2DC\uAD70\uAD6C\uCF54\uB4DC: ")
                    .append(safe(item.getSiGunGuCode()))
                    .append(" / \uC5F0\uB77D\uCC98: ")
                    .append(safe(item.getTel()))
                    .append('\n');
        }
        return sb.toString();
    }

    private String fallbackSummary(List<TourAttractionDto> items) {
        StringBuilder sb = new StringBuilder("\uC624\uB298 \uC218\uC9D1\uB41C \uAD00\uAD11\uC9C0 \uB370\uC774\uD130\uB294 \uCD1D ")
                .append(items.size())
                .append("\uAC74\uC785\uB2C8\uB2E4.");
        int limit = Math.min(items.size(), 3);
        if (limit > 0) {
            sb.append(" \uB300\uD45C \uAD00\uAD11\uC9C0\uB294 ");
            for (int i = 0; i < limit; i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(items.get(i).getTitle());
            }
            sb.append("\uC785\uB2C8\uB2E4.");
        }
        return sb.toString();
    }

    private String buildChangeText(List<TourAttractionChangeDto> items) {
        if (items == null || items.isEmpty()) {
            return "- None";
        }

        StringBuilder sb = new StringBuilder();
        int limit = Math.min(items.size(), 8);
        for (int i = 0; i < limit; i++) {
            TourAttractionChangeDto item = items.get(i);
            sb.append(i + 1)
                    .append(". ")
                    .append(safe(item.getTitle()))
                    .append(" / contentId: ")
                    .append(safe(item.getContentId()))
                    .append(" / contentTypeId: ")
                    .append(safe(item.getContentTypeId()))
                    .append(" / areaCode: ")
                    .append(safe(item.getAreaCode()))
                    .append(" / detail: ")
                    .append(safe(item.getChangeDescription()))
                    .append('\n');
        }
        return sb.toString();
    }

    private String buildContentTypeSummary(TourBatchComparisonResult comparison) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (TourAttractionDto item : comparison.getApiItems()) {
            String key = safe(item.getContentTypeId());
            counts.put(key, counts.getOrDefault(key, 0) + 1);
        }
        if (counts.isEmpty()) {
            return "- None";
        }

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            sb.append("- ")
                    .append(typeLabel(entry.getKey()))
                    .append(": ")
                    .append(entry.getValue())
                    .append(" items\n");
        }
        return sb.toString();
    }

    private String fallbackComparisonSummary(TourBatchComparisonResult comparison) {
        StringBuilder sb = new StringBuilder();
        sb.append("TourAPI에서 수집한 관광지는 총 ")
                .append(comparison.getApiItemCount())
                .append("건입니다. ");
        sb.append("DB 비교 결과 신규 ")
                .append(comparison.getNewItemCount())
                .append("건, 변경 ")
                .append(comparison.getChangedItemCount())
                .append("건, 삭제 후보 ")
                .append(comparison.getMissingItemCount())
                .append("건, 동일 ")
                .append(comparison.getUnchangedItemCount())
                .append("건으로 확인됐습니다.");

        appendRepresentative(sb, " 신규 후보는 ", comparison.getNewItems());
        appendRepresentative(sb, " 변경 후보는 ", comparison.getChangedItems());
        appendRepresentative(sb, " 삭제 후보는 ", comparison.getMissingItems());
        return sb.toString();
    }

    private String typeLabel(String contentTypeId) {
        return switch (safe(contentTypeId)) {
            case "12" -> "12 관광지";
            case "14" -> "14 문화시설";
            case "15" -> "15 행사/공연/축제";
            case "25" -> "25 여행코스";
            case "28" -> "28 레포츠";
            case "32" -> "32 숙박";
            case "38" -> "38 쇼핑";
            case "39" -> "39 음식점";
            default -> contentTypeId;
        };
    }

    private void appendRepresentative(StringBuilder sb, String label, List<TourAttractionChangeDto> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        sb.append(label);
        int limit = Math.min(items.size(), 3);
        for (int i = 0; i < limit; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(items.get(i).getTitle());
        }
        sb.append("입니다.");
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
