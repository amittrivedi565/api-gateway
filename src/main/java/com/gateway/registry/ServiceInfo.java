package com.gateway.registry;

import java.util.List;

public record ServiceInfo(
        String name,
        String host,
        int port,
        String defaultExposure,
        List<RouteInfo> routes
){}

