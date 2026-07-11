package com.neoulteo.routing.resource;

public record RoutingPlaceRequest(
        String key,
        String name,
        Double latitude,
        Double longitude) {
}
