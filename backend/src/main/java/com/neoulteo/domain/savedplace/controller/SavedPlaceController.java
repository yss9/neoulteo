package com.neoulteo.domain.savedplace.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import com.neoulteo.domain.savedplace.dao.DbSavedPlaceDao;
import com.neoulteo.domain.savedplace.dto.SavedPlaceDto;
import com.neoulteo.global.security.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/saved-places")
@Secured("ROLE_USER")
public class SavedPlaceController {
    private final DbSavedPlaceDao savedPlaceDao;

    public SavedPlaceController(DbSavedPlaceDao savedPlaceDao) {
        this.savedPlaceDao = savedPlaceDao;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> list(@AuthenticationPrincipal CustomUserDetails principal) {
        Long userId = currentUserId(principal);
        if (userId == null) {
            return error(HttpStatus.UNAUTHORIZED, "Login is required.");
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", true);
        body.put("items", savedPlaceDao.findByUserId(userId));
        return ResponseEntity.ok(body);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> save(@RequestBody SavedPlaceRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        Long userId = currentUserId(principal);
        if (userId == null) {
            return error(HttpStatus.UNAUTHORIZED, "Login is required.");
        }
        if (request == null || request.attractionContentId() == null) {
            return error(HttpStatus.BAD_REQUEST, "Attraction selection is required.");
        }

        SavedPlaceDto dto = new SavedPlaceDto(userId, request.attractionContentId(), request.sourceType());
        savedPlaceDao.save(dto);
        return ResponseEntity.ok(message(true, "Place saved."));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        Long userId = currentUserId(principal);
        if (userId == null) {
            return error(HttpStatus.UNAUTHORIZED, "Login is required.");
        }

        if (savedPlaceDao.deleteByIdAndUserId(id, userId)) {
            return ResponseEntity.ok(message(true, "Saved place deleted."));
        }
        return error(HttpStatus.NOT_FOUND, "Saved place not found.");
    }

    private Long currentUserId(CustomUserDetails principal) {
        if (principal == null || principal.getUser() == null) {
            return null;
        }
        return principal.getUser().getId();
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

    public record SavedPlaceRequest(Integer attractionContentId, String sourceType) {
    }
}
