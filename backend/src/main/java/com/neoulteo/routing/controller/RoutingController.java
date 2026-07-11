package com.neoulteo.routing.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import com.neoulteo.routing.resource.RoutingPlanRequest;
import com.neoulteo.routing.resource.RoutingPlanResponse;
import com.neoulteo.routing.service.RoutingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/routes")
public class RoutingController {
    private final RoutingService routingService;

    public RoutingController(RoutingService routingService) {
        this.routingService = routingService;
    }

    @PostMapping("/plan")
    public ResponseEntity<RoutingPlanResponse> plan(@RequestBody RoutingPlanRequest request) {
        return ResponseEntity.ok(routingService.buildPlan(request));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> routingError(ResponseStatusException exception) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("message", exception.getReason());
        return ResponseEntity.status(exception.getStatusCode()).body(body);
    }
}
