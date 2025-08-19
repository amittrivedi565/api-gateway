package com.univault.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "services")
public class ServiceConfig {

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
        private String exposure;

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

        public String getExposure() {
            return exposure;
        }

        public void setExposure(String exposure) {
            this.exposure = exposure;
        }
    }
}
