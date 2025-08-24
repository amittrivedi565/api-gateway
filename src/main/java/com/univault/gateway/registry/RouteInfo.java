package com.univault.gateway.registry;

public record RouteInfo(
        String path,
        String method,
        String exposure
) {}
