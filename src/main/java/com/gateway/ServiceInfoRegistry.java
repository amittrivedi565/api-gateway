package com.gateway;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "services")
public class ServiceInfoRegistry {

    private static final Logger log = LoggerFactory.getLogger(ServiceInfoRegistry.class);

    private List<ServiceInfo> list = new ArrayList<>();

    public List<ServiceInfo> getList() { return list; }
    public void setList(List<ServiceInfo> list) { this.list = list; }

    // ANSI color codes
    private static final String RESET = "\u001B[0m";
    private static final String GREEN = "\u001B[32m";
    private static final String CYAN = "\u001B[36m";
    private static final String YELLOW = "\u001B[33m";
    private static final String MAGENTA = "\u001B[35m";

    @PostConstruct
    public void logServices() {
        log.info(GREEN + "Loaded {} services from configuration" + RESET, list.size());
        for (ServiceInfo service : list) {
            log.info(CYAN + "Service: {} at {}:{}" + RESET, service.getName(), service.getHost(), service.getPort());
            log.info(YELLOW + "Default Exposure: {}" + RESET, service.getDefaultExposure());
            for (Route route : service.getRoutes()) {
                log.info(MAGENTA + "Route: {} [{}] -> Exposure: {}" + RESET,
                        route.getPath(), route.getMethod(), route.getExposure());
            }
        }
    }

    public static class ServiceInfo {
        private String name;
        private String host;
        private int port;
        private Exposure defaultExposure;  // use enum here
        private final List<Route> routes = new ArrayList<>();

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }

        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }

        public Exposure getDefaultExposure() { return defaultExposure; }
        public void setDefaultExposure(Exposure defaultExposure) { this.defaultExposure = defaultExposure; }

        public List<Route> getRoutes() { return routes; }
    }

    public static class Route {
        private String path;
        private String method;
        private Exposure exposure; // use enum here

        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }

        public String getMethod() { return method; }
        public void setMethod(String method) { this.method = method; }

        public Exposure getExposure() { return exposure; }
        public void setExposure(Exposure exposure) { this.exposure = exposure; }
    }

    // Define the enum here
    public enum Exposure {
        PUBLIC,
        PRIVATE,
        PROTECTED
    }
}

