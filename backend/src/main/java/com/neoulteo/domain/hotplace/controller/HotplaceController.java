package com.neoulteo.domain.hotplace.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import com.neoulteo.domain.hotplace.dao.DbHotplaceDao;
import com.neoulteo.domain.hotplace.dto.HotplaceDto;
import com.neoulteo.global.security.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hotplaces")
public class HotplaceController {
    private final DbHotplaceDao hotplaceDao;

    public HotplaceController(DbHotplaceDao hotplaceDao) {
        this.hotplaceDao = hotplaceDao;
    }

    @GetMapping
    public Map<String, Object> list(@RequestParam(required = false) String region,
            @RequestParam(required = false) Integer areaCode) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", true);
        if (areaCode != null || hasText(region)) {
            String trimmedRegion = hasText(region) ? region.trim() : null;
            body.put("items", hotplaceDao.findPublicHotplacesByRegion(trimmedRegion, resolveAreaCode(trimmedRegion, areaCode)));
        } else {
            body.put("items", hotplaceDao.findPublicHotplaces());
        }
        return body;
    }

    @GetMapping("/popular")
    public Map<String, Object> popular(@RequestParam(required = false) String region,
            @RequestParam(required = false) Integer areaCode,
            @RequestParam(defaultValue = "5") Integer limit) {
        int normalizedLimit = Math.max(1, Math.min(limit == null ? 5 : limit, 20));
        String trimmedRegion = hasText(region) ? region.trim() : null;

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", true);
        body.put("items", hotplaceDao.findPopularHotplaces(
                trimmedRegion,
                resolveAreaCode(trimmedRegion, areaCode),
                normalizedLimit));
        return body;
    }

    @GetMapping("/me")
    @Secured("ROLE_USER")
    public ResponseEntity<Map<String, Object>> mine(@AuthenticationPrincipal CustomUserDetails principal) {
        if (principal == null || principal.getUser().getId() == null) {
            return error(HttpStatus.UNAUTHORIZED, "Login is required.");
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", true);
        body.put("items", hotplaceDao.findByUserId(principal.getUser().getId()));
        return ResponseEntity.ok(body);
    }

    @PostMapping
    @Secured("ROLE_USER")
    public ResponseEntity<Map<String, Object>> register(@RequestBody HotplaceRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        if (principal == null) {
            return error(HttpStatus.UNAUTHORIZED, "Login is required.");
        }

        Long userId = principal.getUser().getId();
        if (userId == null) {
            return error(HttpStatus.UNAUTHORIZED, "Login is required.");
        }

        if (request.attractionContentId() == null) {
            return error(HttpStatus.BAD_REQUEST, "Attraction selection is required.");
        }

        if (hotplaceDao.existsByUserIdAndAttractionContentId(userId, request.attractionContentId())) {
            return error(HttpStatus.BAD_REQUEST, "This attraction is already registered as your hotplace.");
        }

        HotplaceDto dto = new HotplaceDto(
                userId,
                request.attractionContentId(),
                request.date(),
                request.description());

        if (hotplaceDao.save(dto)) {
            return ResponseEntity.ok(message(true, "Hotplace registered."));
        }
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to register hotplace.");
    }

    @PatchMapping("/{id}")
    @Secured("ROLE_USER")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @RequestBody HotplaceUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        Long userId = currentUserId(principal);
        if (userId == null) {
            return error(HttpStatus.UNAUTHORIZED, "Login is required.");
        }

        HotplaceDto dto = new HotplaceDto();
        dto.setId(id);
        dto.setUserId(userId);
        dto.setDate(request.date());
        dto.setDescription(request.description());

        if (hotplaceDao.updateByIdAndUserId(dto)) {
            return ResponseEntity.ok(message(true, "Hotplace updated."));
        }
        return error(HttpStatus.NOT_FOUND, "Hotplace not found.");
    }

    @DeleteMapping("/{id}")
    @Secured("ROLE_USER")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        Long userId = currentUserId(principal);
        if (userId == null) {
            return error(HttpStatus.UNAUTHORIZED, "Login is required.");
        }

        if (hotplaceDao.deleteByIdAndUserId(id, userId)) {
            return ResponseEntity.ok(message(true, "Hotplace deleted."));
        }
        return error(HttpStatus.NOT_FOUND, "Hotplace not found.");
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

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private Long currentUserId(CustomUserDetails principal) {
        if (principal == null || principal.getUser() == null) {
            return null;
        }
        return principal.getUser().getId();
    }

    private Integer areaCodeForRegion(String region) {
        if (region == null) {
            return null;
        }
        return switch (region) {
            case "\uC11C\uC6B8", "\uC11C\uC6B8\uD2B9\uBCC4\uC2DC" -> 1;
            case "\uC778\uCC9C", "\uC778\uCC9C\uAD11\uC5ED\uC2DC" -> 2;
            case "\uB300\uC804", "\uB300\uC804\uAD11\uC5ED\uC2DC" -> 3;
            case "\uB300\uAD6C", "\uB300\uAD6C\uAD11\uC5ED\uC2DC" -> 4;
            case "\uAD11\uC8FC", "\uAD11\uC8FC\uAD11\uC5ED\uC2DC" -> 5;
            case "\uBD80\uC0B0", "\uBD80\uC0B0\uAD11\uC5ED\uC2DC" -> 6;
            case "\uC6B8\uC0B0", "\uC6B8\uC0B0\uAD11\uC5ED\uC2DC" -> 7;
            case "\uC138\uC885", "\uC138\uC885\uD2B9\uBCC4\uC790\uCE58\uC2DC" -> 8;
            case "\uACBD\uAE30", "\uACBD\uAE30\uB3C4" -> 31;
            case "\uAC15\uC6D0", "\uAC15\uC6D0\uB3C4", "\uAC15\uC6D0\uD2B9\uBCC4\uC790\uCE58\uB3C4" -> 32;
            case "\uCDA9\uBD81", "\uCDA9\uCCAD\uBD81\uB3C4" -> 33;
            case "\uCDA9\uB0A8", "\uCDA9\uCCAD\uB0A8\uB3C4" -> 34;
            case "\uACBD\uBD81", "\uACBD\uC0C1\uBD81\uB3C4" -> 35;
            case "\uACBD\uB0A8", "\uACBD\uC0C1\uB0A8\uB3C4" -> 36;
            case "\uC804\uBD81", "\uC804\uB77C\uBD81\uB3C4", "\uC804\uBD81\uD2B9\uBCC4\uC790\uCE58\uB3C4" -> 37;
            case "\uC804\uB0A8", "\uC804\uB77C\uB0A8\uB3C4" -> 38;
            case "\uC81C\uC8FC", "\uC81C\uC8FC\uB3C4", "\uC81C\uC8FC\uD2B9\uBCC4\uC790\uCE58\uB3C4" -> 39;
            default -> null;
        };
    }

    private Integer resolveAreaCode(String region, Integer areaCode) {
        return areaCode != null ? areaCode : areaCodeForRegion(region);
    }

    public record HotplaceRequest(Integer attractionContentId, String date, String description) {
    }

    public record HotplaceUpdateRequest(String date, String description) {
    }
}
