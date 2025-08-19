package com.univault.gateway.registry;

import java.util.Map;

// InstanceInfo info = new InstanceInfo(
//     "abc-service",
//     "00.0.0.00",
//     8080,
//     Map.of("version", "1.0", "zone", "us-east")
// );

public record InstanceInfo(String service, String host, int port, Map<String, String> metadata) {}
