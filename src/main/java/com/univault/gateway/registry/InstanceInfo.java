package com.univault.gateway.registry;

import java.util.Map;

/*
    service: "abc-service",
    host: "localhost",
    port: 8081,
    exposure:protected
 * */
public record InstanceInfo(String service, String host, int port, String exposure){}
