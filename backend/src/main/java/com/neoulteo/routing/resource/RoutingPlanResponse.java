package com.neoulteo.routing.resource;

import java.util.List;

public record RoutingPlanResponse(
        boolean success,
        String mode,
        String provider,
        List<String> orderedKeys,
        List<RoutingPoint> path,
        long distanceMeters,
        long durationSeconds) {
}
