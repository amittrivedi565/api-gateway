package com.gateway.config;

/*
 * RouteInfo POJO
 */
public class RouteInfo {
    private String path;
    private String method;
    private String exposure;

    public RouteInfo() {
    }

    public RouteInfo(String path, String method, String exposure) {
        this.path = path;
        this.method = method;
        this.exposure = exposure;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getExposure() {
        return exposure;
    }

    public void setExposure(String exposure) {
        this.exposure = exposure;
    }
}
