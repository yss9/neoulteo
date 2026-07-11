package com.neoulteo.routing.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.neoulteo.routing.resource.RoutingPlaceRequest;
import com.neoulteo.routing.resource.RoutingPlanRequest;
import com.neoulteo.routing.resource.RoutingPlanResponse;
import com.neoulteo.routing.resource.RoutingPoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RoutingService {
    private static final int MAX_PLACES = 10;

    private final String tmapAppKey;
    private final RestClient restClient;

    public RoutingService(
            @Value("${neoulteo.routing.tmap-app-key:}") String tmapAppKey,
            RestClient.Builder restClientBuilder) {
        this.tmapAppKey = tmapAppKey;
        this.restClient = restClientBuilder.baseUrl("https://apis.openapi.sk.com").build();
    }

    public RoutingPlanResponse buildPlan(RoutingPlanRequest request) {
        if (!StringUtils.hasText(tmapAppKey)) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "TMAP 길찾기 키가 없습니다. 서버에 TMAP_APP_KEY를 설정해 주세요.");
        }

        String mode = normalizeMode(request == null ? null : request.mode());
        List<RoutingPlaceRequest> places = validatePlaces(request == null ? null : request.places());
        Map<RouteKey, RouteSegment> cache = new HashMap<>();
        List<RoutingPlaceRequest> ordered = Boolean.TRUE.equals(request.optimize())
                ? optimize(places, mode, cache)
                : new ArrayList<>(places);

        List<RoutingPoint> path = new ArrayList<>();
        long totalDistance = 0;
        long totalDuration = 0;
        for (int index = 1; index < ordered.size(); index += 1) {
            RouteSegment segment = route(ordered.get(index - 1), ordered.get(index), mode, cache);
            appendPath(path, segment.path());
            totalDistance += segment.distanceMeters();
            totalDuration += segment.durationSeconds();
        }

        return new RoutingPlanResponse(
                true,
                mode,
                "TMAP",
                ordered.stream().map(RoutingPlaceRequest::key).toList(),
                path,
                totalDistance,
                totalDuration);
    }

    private List<RoutingPlaceRequest> optimize(List<RoutingPlaceRequest> places, String mode,
            Map<RouteKey, RouteSegment> cache) {
        if (places.size() < 3) {
            return new ArrayList<>(places);
        }

        List<RoutingPlaceRequest> remaining = new ArrayList<>(places);
        List<RoutingPlaceRequest> ordered = new ArrayList<>();
        RoutingPlaceRequest current = remaining.remove(0);
        ordered.add(current);

        while (!remaining.isEmpty()) {
            RoutingPlaceRequest nearest = null;
            long nearestDistance = Long.MAX_VALUE;
            for (RoutingPlaceRequest candidate : remaining) {
                RouteSegment segment = route(current, candidate, mode, cache);
                if (segment.distanceMeters() < nearestDistance) {
                    nearest = candidate;
                    nearestDistance = segment.distanceMeters();
                }
            }
            current = nearest;
            ordered.add(current);
            remaining.remove(current);
        }
        return ordered;
    }

    private RouteSegment route(RoutingPlaceRequest from, RoutingPlaceRequest to, String mode,
            Map<RouteKey, RouteSegment> cache) {
        RouteKey key = new RouteKey(from.longitude(), from.latitude(), to.longitude(), to.latitude(), mode);
        RouteSegment cached = cache.get(key);
        if (cached != null) return cached;

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("startX", String.valueOf(from.longitude()));
        body.put("startY", String.valueOf(from.latitude()));
        body.put("endX", String.valueOf(to.longitude()));
        body.put("endY", String.valueOf(to.latitude()));
        body.put("reqCoordType", "WGS84GEO");
        body.put("resCoordType", "WGS84GEO");
        body.put("startName", displayName(from));
        body.put("endName", displayName(to));
        body.put("searchOption", "CAR".equals(mode) ? "2" : "0");

        String endpoint = "CAR".equals(mode)
                ? "/tmap/routes?version=1&format=json"
                : "/tmap/routes/pedestrian?version=1&format=json";
        JsonNode response;
        try {
            response = restClient.post()
                    .uri(endpoint)
                    .header("appKey", tmapAppKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "TMAP 길찾기 요청에 실패했습니다: " + displayName(from) + " → " + displayName(to), exception);
        }

        RouteSegment segment = parseSegment(response);
        cache.put(key, segment);
        return segment;
    }

    private RouteSegment parseSegment(JsonNode response) {
        JsonNode features = response == null ? null : response.path("features");
        if (features == null || !features.isArray() || features.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "TMAP에서 유효한 경로를 받지 못했습니다.");
        }

        List<RoutingPoint> path = new ArrayList<>();
        long totalDistance = 0;
        long totalDuration = 0;
        long lineDistance = 0;
        for (JsonNode feature : features) {
            JsonNode properties = feature.path("properties");
            totalDistance = Math.max(totalDistance, properties.path("totalDistance").asLong(0));
            totalDuration = Math.max(totalDuration, properties.path("totalTime").asLong(0));
            lineDistance += properties.path("distance").asLong(0);

            JsonNode geometry = feature.path("geometry");
            if (!"LineString".equals(geometry.path("type").asText())) continue;
            for (JsonNode coordinate : geometry.path("coordinates")) {
                if (!coordinate.isArray() || coordinate.size() < 2) continue;
                double longitude = coordinate.get(0).asDouble();
                double latitude = coordinate.get(1).asDouble();
                appendPoint(path, new RoutingPoint(latitude, longitude));
            }
        }

        if (path.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "TMAP 경로 좌표가 비어 있습니다.");
        }
        if (totalDistance <= 0) totalDistance = lineDistance;
        return new RouteSegment(path, totalDistance, totalDuration);
    }

    private void appendPath(List<RoutingPoint> target, List<RoutingPoint> source) {
        source.forEach(point -> appendPoint(target, point));
    }

    private void appendPoint(List<RoutingPoint> target, RoutingPoint point) {
        if (!target.isEmpty()) {
            RoutingPoint last = target.get(target.size() - 1);
            if (Double.compare(last.latitude(), point.latitude()) == 0
                    && Double.compare(last.longitude(), point.longitude()) == 0) return;
        }
        target.add(point);
    }

    private String normalizeMode(String value) {
        String mode = StringUtils.hasText(value) ? value.trim().toUpperCase(Locale.ROOT) : "CAR";
        if (!"CAR".equals(mode) && !"WALK".equals(mode)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이동 수단은 CAR 또는 WALK만 사용할 수 있습니다.");
        }
        return mode;
    }

    private List<RoutingPlaceRequest> validatePlaces(List<RoutingPlaceRequest> places) {
        if (places == null || places.size() < 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "경로를 만들려면 관광지가 2곳 이상 필요합니다.");
        }
        if (places.size() > MAX_PLACES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "한 일차의 경로는 관광지 " + MAX_PLACES + "곳까지 계산할 수 있습니다.");
        }

        for (RoutingPlaceRequest place : places) {
            if (place == null || !StringUtils.hasText(place.key()) || place.latitude() == null
                    || place.longitude() == null || !Double.isFinite(place.latitude())
                    || !Double.isFinite(place.longitude()) || place.latitude() < -90 || place.latitude() > 90
                    || place.longitude() < -180 || place.longitude() > 180) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "관광지 좌표가 올바르지 않습니다.");
            }
        }
        return new ArrayList<>(places);
    }

    private String displayName(RoutingPlaceRequest place) {
        return StringUtils.hasText(place.name()) ? place.name() : place.key();
    }

    private record RouteKey(double fromLongitude, double fromLatitude, double toLongitude, double toLatitude,
            String mode) {
    }

    private record RouteSegment(List<RoutingPoint> path, long distanceMeters, long durationSeconds) {
    }
}
