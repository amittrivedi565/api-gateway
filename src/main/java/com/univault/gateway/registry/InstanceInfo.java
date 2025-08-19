package com.univault.gateway.registry;

import java.util.Map;

/*
    service: "abc-service",
    host: "localhost",
    port: 8081,
    exposure: public or protected
    meta:[]
 * */
public record InstanceInfo(String service, String host, int port, String exposure,Map<String, String> metadata) {}
