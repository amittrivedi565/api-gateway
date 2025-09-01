package com.gateway.config;

import java.util.List;

/*
 * ServiceInfo POJO
 */
public class ServiceInfo {
    private String name;
    private String host;
    private int port;
    private String defaultExposure;
    private List<RouteInfo> routes;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getDefaultExposure() {
        return defaultExposure;
    }

    public void setDefaultExposure(String defaultExposure) {
        this.defaultExposure = defaultExposure;
    }

    public List<RouteInfo> getRoutes() {
        return routes;
    }

    public void setRoutes(List<RouteInfo> routes) {
        this.routes = routes;
    }
}
