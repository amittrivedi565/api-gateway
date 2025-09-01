package com.gateway;

import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class Utils {

    /*
     * By providing HttpServlet incoming request url we can extract service name for redirection
     * */
    public String extractServiceNameFromUrl(String url) {
        if (url == null) {
            throw new IllegalArgumentException("URL cannot be null");
        }

        /*
         * Example: incoming url = localhost:4000/service-name/api/endpoint
         * Split by "/" -> ["", "service-name", "api", "endpoint"]
         * Filter out blanks -> ["service-name", "api", "endpoint"]
         * Service name is parts[0]
         */
        String[] parts = Arrays.stream(url.split("/"))
                .filter(s -> !s.isBlank())
                .toArray(String[]::new);

        if (parts.length == 0) {
            throw new IllegalArgumentException("No service name found in URL: " + url);
        }
        return parts[0];
    }

    /*
     * Helper method to find service info from in-memory YAML config
     */
    public Registry.Service getService(String serviceName, Registry registry) {
        return registry.getList()
                .stream()
                .filter(s -> s.getName().equals(serviceName))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Service Unavailable: " + serviceName));
    }

    public String removeServiceNameFromUrl(String url){
        return url.replaceFirst("/[^/]+", "");
    }

    /*
     * Build target url from the existing request, e.g., http://localhost:8080/institutes/abc
     */
    public String createTargetUrl(Registry.Service service, String url) {
        return "http://" + service.getHost() + ":" + service.getPort() + url;
    }
}
