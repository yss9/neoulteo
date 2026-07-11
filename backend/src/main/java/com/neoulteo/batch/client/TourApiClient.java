package com.neoulteo.batch.client;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.neoulteo.batch.dto.TourAttractionDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class TourApiClient {
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${tour.api.base-url}")
    private String baseUrl;

    @Value("${tour.api.service-key}")
    private String serviceKey;

    @Value("${tour.api.mobile-os:ETC}")
    private String mobileOs;

    @Value("${tour.api.mobile-app:Neoulteo}")
    private String mobileApp;

    @Value("${tour.api.num-of-rows:30}")
    private int numOfRows;

    @Value("${tour.api.max-pages:0}")
    private int maxPages;

    @Value("${tour.api.area-code:6}")
    private String areaCode;

    @Value("${tour.api.content-type-id:12}")
    private String contentTypeId;

    @Value("${tour.api.content-type-ids:}")
    private String contentTypeIds;

    public TourApiClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(10));
        requestFactory.setReadTimeout(Duration.ofSeconds(30));
        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    public List<TourAttractionDto> fetchAreaBasedAttractions() throws Exception {
        return fetchAreaBasedAttractions(contentTypeId);
    }

    public List<TourAttractionDto> fetchConfiguredContentTypeAttractions() throws Exception {
        Map<String, TourAttractionDto> merged = new LinkedHashMap<>();
        for (String targetContentTypeId : getContentTypeIds()) {
            System.out.println("[TourBatch] contentTypeId=" + targetContentTypeId + " fetch started.");
            List<TourAttractionDto> items = fetchAreaBasedAttractions(targetContentTypeId);
            for (TourAttractionDto item : items) {
                merged.put(item.getContentId(), item);
            }
            System.out.println("[TourBatch] contentTypeId=" + targetContentTypeId
                    + " fetch completed. items=" + items.size());
        }
        return new ArrayList<>(merged.values());
    }

    public List<String> getContentTypeIds() {
        if (hasText(contentTypeIds)) {
            return Arrays.stream(contentTypeIds.split(","))
                    .map(String::trim)
                    .filter(this::hasText)
                    .distinct()
                    .toList();
        }
        return hasText(contentTypeId) ? List.of(contentTypeId) : List.of();
    }

    public List<TourAttractionDto> fetchAreaBasedAttractions(String targetContentTypeId) throws Exception {
        if (serviceKey == null || serviceKey.isBlank()) {
            throw new IllegalStateException("tour.api.service-key is required.");
        }

        List<TourAttractionDto> allItems = new ArrayList<>();
        int pageNo = 1;
        int totalCount = 0;
        do {
            if (pageNo == 1 || pageNo % 20 == 0) {
                System.out.println("[TourBatch] fetching TourAPI contentTypeId=" + targetContentTypeId
                        + ", page=" + pageNo
                        + ", fetched=" + allItems.size()
                        + (totalCount > 0 ? ", totalCount=" + totalCount : ""));
            }
            String json;
            try {
                json = restClient.get()
                        .uri(buildAreaBasedUri(pageNo, targetContentTypeId))
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .body(String.class);
            } catch (HttpClientErrorException.TooManyRequests e) {
                if (!allItems.isEmpty()) {
                    System.out.println("[TourBatch] TourAPI quota exceeded. Continue with partial items. fetched="
                            + allItems.size());
                    break;
                }
                throw e;
            }

            if (json == null || json.isBlank()) {
                break;
            }

            if (pageNo == 1) {
                totalCount = parseTotalCount(json);
            }
            allItems.addAll(parseItems(json));
            pageNo++;
        } while (shouldFetchNextPage(pageNo, totalCount, allItems.size()));

        System.out.println("[TourBatch] TourAPI fetch completed. fetched=" + allItems.size()
                + ", totalCount=" + totalCount);
        return allItems;
    }

    private URI buildAreaBasedUri(int pageNo, String targetContentTypeId) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl)
                .queryParam("serviceKey", serviceKey)
                .queryParam("MobileOS", mobileOs)
                .queryParam("MobileApp", mobileApp)
                .queryParam("_type", "json")
                .queryParam("arrange", "Q")
                .queryParam("numOfRows", numOfRows)
                .queryParam("pageNo", pageNo);
        if (hasText(areaCode)) {
            builder.queryParam("areaCode", areaCode);
        }
        if (hasText(targetContentTypeId)) {
            builder.queryParam("contentTypeId", targetContentTypeId);
        }

        return builder.build(true).toUri();
    }

    public String getAreaCode() {
        return areaCode;
    }

    public String getContentTypeId() {
        return contentTypeId;
    }

    private List<TourAttractionDto> parseItems(String json) throws Exception {
        JsonNode root = objectMapper.readTree(json);
        String resultCode = root.at("/response/header/resultCode").asText("");
        String resultMsg = root.at("/response/header/resultMsg").asText("");
        if (!resultCode.isBlank() && !"0000".equals(resultCode)) {
            throw new IllegalStateException("TourAPI failed: " + resultCode + " " + resultMsg);
        }

        JsonNode itemNode = root.at("/response/body/items/item");
        if (itemNode == null || itemNode.isMissingNode() || itemNode.isNull()) {
            System.out.println("[TourBatch] TourAPI returned no items. resultCode=" + resultCode
                    + ", resultMsg=" + resultMsg);
            return List.of();
        }

        List<TourAttractionDto> items = new ArrayList<>();
        if (itemNode.isArray()) {
            for (JsonNode node : itemNode) {
                items.add(mapItem(node));
            }
        } else {
            items.add(mapItem(itemNode));
        }
        return new ArrayList<>(items.stream()
                .filter(item -> hasText(item.getContentId()) && hasText(item.getTitle()))
                .toList());
    }

    private int parseTotalCount(String json) throws Exception {
        JsonNode root = objectMapper.readTree(json);
        return root.at("/response/body/totalCount").asInt(0);
    }

    private boolean shouldFetchNextPage(int nextPageNo, int totalCount, int fetchedCount) {
        if (totalCount <= 0 || fetchedCount >= totalCount) {
            return false;
        }
        if (maxPages > 0 && nextPageNo > maxPages) {
            System.out.println("[TourBatch] stopped by tour.api.max-pages=" + maxPages
                    + ", fetched=" + fetchedCount + ", totalCount=" + totalCount);
            return false;
        }
        return true;
    }

    private TourAttractionDto mapItem(JsonNode node) {
        TourAttractionDto item = new TourAttractionDto();
        item.setContentId(text(node, "contentid"));
        item.setTitle(text(node, "title"));
        item.setAddr1(text(node, "addr1"));
        item.setFirstImage1(text(node, "firstimage"));
        item.setLatitude(text(node, "mapy"));
        item.setLongitude(text(node, "mapx"));
        item.setContentTypeId(text(node, "contenttypeid"));
        item.setAreaCode(text(node, "areacode"));
        item.setSiGunGuCode(text(node, "sigungucode"));
        item.setTel(text(node, "tel"));
        return item;
    }

    private String text(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);
        if (value == null || value.isNull()) {
            return "";
        }
        String text = value.asText("");
        return "null".equalsIgnoreCase(text.trim()) ? "" : text;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
