package com.gateway;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class Utils {

    private static final Logger log = LoggerFactory.getLogger(Utils.class);

    /*
     * @isInternalIp checks if the request is coming from the internal network or
     * not
     * since it is internal network, ip addresses will start with certain suffixes
     */
    public boolean isInternalIp(String ip) {
        return ip.startsWith("10.")
                || ip.startsWith("192.168.")
                || ip.startsWith("127.")
                || ip.equals("0:0:0:0:0:0:0:1")
                || ip.equals("::1");
    }


    /*
     * @isAuthenticated validates token provided by the user, incoming with the
     * request
     * It calls the auth service registered and validates
     * We extract the token either from the `Header` or `Cookies`
     * 
     * 
     * If no token return false
     * 
     */
    public boolean isAuthenticated(HttpServletRequest request, RestTemplate restTemplate, Registry registry) {
        try {
            // 1. Extract token from Authorization header
            String token = null;

            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }

            // 2. If no header token, check cookies
            if (token == null && request.getCookies() != null) {
                for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                    if ("AuthToken".equals(cookie.getName())) {
                        token = cookie.getValue();
                        break;
                    }
                }
            }

            // 3. No token found
            if (token == null)
                return false;

            // 4. Call Auth Service to verify token
            Registry.Service service = getService("auth", registry);

            String route = getSpecificRouteFromRegistry(service, "/auth/token");

            String authEndpoint = createTargetUrl(service, route);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(authEndpoint, HttpMethod.POST, entity,
                    String.class);

            return response.getStatusCode() == HttpStatus.OK;

        } catch (HttpClientErrorException.Unauthorized e) {
            log.warn("Unauthorized: Token rejected by Auth service. {}", e.getMessage());
            return false; 
        } catch (HttpClientErrorException.NotFound e) {
            log.error("Auth endpoint not found: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Unexpected error while authenticating token: {}", e.getMessage(), e);
            return false;
        }
    }

    /*
     * By providing HttpServlet incoming request url we can extract service name for
     * redirection
     */
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
     * Find service by name and registry
     */
    public Registry.Service getService(String serviceName, Registry registry) {
        return registry.getList()
                .stream()
                .filter(s -> s.getName().equals(serviceName))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Service Unavailable: " + serviceName));
    }

        /*
     * It finds a specific route by service & route name
     * Mainly utilized by @isAuthenticated
    */
    public String getSpecificRouteFromRegistry(Registry.Service service, String route) {
        service.getRoutes()
                .stream()
                .filter(s -> s.getPath().equals(route))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Route not found: " + route + "for service" + "  " + service.getName()));

        return route;

    }

    /*
     * Removes service name from the url
     * It is required by create target / destination url
     */
    public String removeServiceNameFromUrl(String url) {
        return url.replaceFirst("/[^/]+", "");
    }

    /*
     * Build target url from the existing request, e.g.,
     * http://localhost:8080/institutes/abc
     */
    public String createTargetUrl(Registry.Service service, String url) {
        return "http://" + service.getHost() + ":" + service.getPort() + url;
    }

}
