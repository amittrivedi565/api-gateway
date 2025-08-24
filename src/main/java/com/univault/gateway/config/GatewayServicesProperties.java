package com.univault.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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
        private String defaultExposure; // new default-exposure
        private List<Route> routes = new ArrayList<>(); // list of route-specific exposures

        // getters & setters
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

        public List<Route> getRoutes() {
            return routes;
        }

        public void setRoutes(List<Route> routes) {
            this.routes = routes;
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
}
