package com.neoulteo.routing.resource;

import java.util.List;

public record RoutingPlanRequest(
        String mode,
        Boolean optimize,
        List<RoutingPlaceRequest> places) {
}
