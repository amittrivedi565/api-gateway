package com.gateway.registry;

public record RouteInfo(
        String path,
        String method,
        String exposure
) {}
