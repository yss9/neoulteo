package com.neoulteo.domain.travelplan.controller;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.neoulteo.domain.travelplan.dao.DbTravelPlanDao;
import com.neoulteo.domain.travelplan.dto.TravelPlanDto;
import com.neoulteo.domain.travelplan.dto.TravelPlanPlaceDto;
import com.neoulteo.global.security.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/plans")
public class TravelPlanController {
    private final DbTravelPlanDao travelPlanDao;

    public TravelPlanController(DbTravelPlanDao travelPlanDao) {
        this.travelPlanDao = travelPlanDao;
    }

    @GetMapping
    @Secured("ROLE_USER")
    public ResponseEntity<Map<String, Object>> list(@AuthenticationPrincipal CustomUserDetails principal) {
        if (principal == null) {
            return error(HttpStatus.UNAUTHORIZED, "Login is required.");
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", true);
        body.put("items", travelPlanDao.findByWriterEmail(principal.getUsername()));
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TravelPlanDto> get(@PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        TravelPlanDto plan = travelPlanDao.findById(id);
        if (plan == null) {
            return ResponseEntity.notFound().build();
        }
        if (!plan.isShared() && !isOwner(plan, principal)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(plan);
    }

    @GetMapping("/shared/{id}")
    public ResponseEntity<TravelPlanDto> getShared(@PathVariable String id) {
        TravelPlanDto plan = travelPlanDao.findSharedById(id);
        if (plan == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(plan);
    }

    @PostMapping
    @Secured("ROLE_USER")
    public ResponseEntity<Map<String, Object>> save(@RequestBody TravelPlanRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        if (principal == null) {
            return error(HttpStatus.UNAUTHORIZED, "Login is required.");
        }

        String email = principal.getUsername();
        List<TravelPlanPlaceDto> normalizedPlaces = normalizePlaces(request);
        if (normalizedPlaces.isEmpty()) {
            return error(HttpStatus.BAD_REQUEST, "No places to save.");
        }

        TravelPlanDto dto = new TravelPlanDto(
                String.valueOf(System.currentTimeMillis()),
                email,
                normalizeTitle(request),
                normalizeDurationDays(request, normalizedPlaces),
                normalizedPlaces,
                new Date().toString());

        if (travelPlanDao.save(dto)) {
            return ResponseEntity.ok(message(true, "Travel plan saved."));
        }
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save travel plan.");
    }

    @PatchMapping("/{id}")
    @Secured("ROLE_USER")
    public ResponseEntity<Map<String, Object>> update(@PathVariable String id, @RequestBody TravelPlanRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        if (principal == null) {
            return error(HttpStatus.UNAUTHORIZED, "Login is required.");
        }

        List<TravelPlanPlaceDto> normalizedPlaces = normalizePlaces(request);
        if (normalizedPlaces.isEmpty()) {
            return error(HttpStatus.BAD_REQUEST, "No places to save.");
        }

        TravelPlanDto dto = new TravelPlanDto(
                id,
                principal.getUsername(),
                normalizeTitle(request),
                normalizeDurationDays(request, normalizedPlaces),
                normalizedPlaces,
                new Date().toString());

        if (travelPlanDao.update(dto)) {
            return ResponseEntity.ok(message(true, "Travel plan updated."));
        }
        return error(HttpStatus.NOT_FOUND, "Travel plan not found.");
    }

    @PatchMapping("/{id}/share")
    @Secured("ROLE_USER")
    public ResponseEntity<Map<String, Object>> updateShared(@PathVariable String id,
            @RequestBody ShareRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        if (principal == null) {
            return error(HttpStatus.UNAUTHORIZED, "Login is required.");
        }

        boolean shared = request != null && request.shared();
        if (!travelPlanDao.updateShared(id, principal.getUsername(), shared)) {
            return error(HttpStatus.NOT_FOUND, "Travel plan not found.");
        }

        Map<String, Object> body = message(true, shared ? "Travel plan sharing enabled." : "Travel plan sharing disabled.");
        body.put("id", id);
        body.put("shared", shared);
        body.put("shareCode", shared ? id : "");
        return ResponseEntity.ok(body);
    }

    @PostMapping("/{id}/copy")
    @Secured("ROLE_USER")
    public ResponseEntity<Map<String, Object>> copyShared(@PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        if (principal == null) {
            return error(HttpStatus.UNAUTHORIZED, "Login is required.");
        }

        TravelPlanDto source = travelPlanDao.findSharedById(id);
        if (source == null) {
            return error(HttpStatus.NOT_FOUND, "Shared travel plan not found.");
        }

        TravelPlanDto copied = new TravelPlanDto(
                String.valueOf(System.currentTimeMillis()),
                principal.getUsername(),
                source.getTitle(),
                source.getDurationDays(),
                source.getPlaces(),
                new Date().toString());

        if (!travelPlanDao.save(copied)) {
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to copy travel plan.");
        }

        Map<String, Object> body = message(true, "Shared travel plan copied.");
        body.put("id", copied.getId());
        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/{id}")
    @Secured("ROLE_USER")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        if (principal == null) {
            return error(HttpStatus.UNAUTHORIZED, "Login is required.");
        }

        if (travelPlanDao.deleteByIdAndWriterEmail(id, principal.getUsername())) {
            return ResponseEntity.ok(message(true, "Travel plan deleted."));
        }
        return error(HttpStatus.NOT_FOUND, "Travel plan not found.");
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(message(false, message));
    }

    private Map<String, Object> message(boolean success, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", success);
        body.put("message", message);
        return body;
    }

    private boolean isOwner(TravelPlanDto plan, CustomUserDetails principal) {
        return principal != null
                && principal.getUsername() != null
                && principal.getUsername().equals(plan.getWriterEmail());
    }

    private List<TravelPlanPlaceDto> normalizePlaces(TravelPlanRequest request) {
        if (request == null || request.places() == null) {
            return List.of();
        }

        return request.places().stream()
                .filter(place -> place != null && place.attractionContentId() != null && hasText(place.name()))
                .map(place -> new TravelPlanPlaceDto(place.attractionContentId(), normalizeDayNo(place.dayNo()),
                        place.name().trim()))
                .toList();
    }

    private int normalizeDayNo(Integer dayNo) {
        return dayNo == null || dayNo < 1 ? 1 : dayNo;
    }

    private String normalizeTitle(TravelPlanRequest request) {
        if (request == null || !hasText(request.title())) {
            return null;
        }
        String title = request.title().trim();
        return title.length() > 100 ? title.substring(0, 100) : title;
    }

    private int normalizeDurationDays(TravelPlanRequest request, List<TravelPlanPlaceDto> places) {
        int maxDayNo = places.stream().mapToInt(TravelPlanPlaceDto::getDayNo).max().orElse(1);
        if (request == null || request.durationDays() == null) {
            return maxDayNo;
        }
        return Math.max(request.durationDays(), maxDayNo);
    }

    public record TravelPlanRequest(String title, Integer durationDays, List<TravelPlanPlaceRequest> places) {
    }

    public record TravelPlanPlaceRequest(Integer attractionContentId, Integer dayNo, String name) {
    }

    public record ShareRequest(boolean shared) {
    }
}
