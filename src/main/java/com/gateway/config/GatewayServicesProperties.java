package com.gateway.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "services")
public class GatewayServicesProperties {

    private List<ServiceInfo> list = new ArrayList<>();

    public List<ServiceInfo> getList() {
        return list;
    }

    public void setList(List<ServiceInfo> list) {
        this.list = list;
    }

    public static class ServiceInfo {
        private String name;
        private String host;
        private int port;
        private String defaultExposure; // binds default-exposure in YAML
        private final List<Route> routes = new ArrayList<>();

        // Getters
        public String getName() {
            return name;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }

        public String getDefaultExposure() {
            return defaultExposure;
        }

        public List<Route> getRoutes() {
            return routes;
        }

        // Setters (required for Spring Boot binding)
        public void setName(String name) {
            this.name = name;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public void setDefaultExposure(String defaultExposure) {
            this.defaultExposure = defaultExposure;
        }
    }

    public static class Route {
        private String path;
        private String method;
        private String exposure;

        // getters & setters
        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public void setExposure(String exposure) {
            this.exposure = exposure;
        }

        public String getMethod() {
            return method;
        }

        public String getExposure() {
            return exposure;
        }

    }
}
