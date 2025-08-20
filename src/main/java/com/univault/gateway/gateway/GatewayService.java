package com.univault.gateway.gateway;

import com.univault.gateway.registry.InstanceInfo;
import com.univault.gateway.registry.RegistryService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GatewayService {

    private static final Logger log = LoggerFactory.getLogger(GatewayService.class);

    private final RegistryService registryService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Autowired
    public GatewayService(RegistryService registryService) {
        this.registryService = registryService;
    }

    public String resolveRoute(String serviceName, String relativePath) {
        List<InstanceInfo> instances = registryService.getInstances(serviceName);
        if (instances.isEmpty()) {
            log.error("No instance found for service: {}", serviceName);
            throw new RuntimeException("No instance found for service: " + serviceName);
        }
        InstanceInfo instance = instances.get(0);
        return "http://" + instance.host() + ":" + instance.port() + relativePath;
    }

    public ResponseEntity<?> forwardRequest(String targetUrl,
                                            HttpServletRequest request,
                                            String method,
                                            String serviceName) {
        try {
            // Read body
            String body = request.getReader().lines().collect(Collectors.joining("\n"));

            // Headers with token
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String token = request.getHeader("Authorization");
            if (token != null) headers.set("Authorization", token);

            HttpEntity<String> entity = new HttpEntity<>(body, headers);

            // Get service info
            InstanceInfo instance = registryService.getInstances(serviceName).stream().findFirst()
                    .orElseThrow(() -> new RuntimeException("Service unavailable: " + serviceName));

            // Strip /gateway prefix
            String path = request.getRequestURI().replaceFirst("^/gateway", "");

            boolean isProtected = "protected".equalsIgnoreCase(instance.exposure());
            boolean isPublic = isPublicPath(path, method, instance.publicPaths());

            // Auth only for protected + non-public
            if (isProtected && !isPublic) {
                InstanceInfo auth = registryService.getInstances("auth-service").stream().findFirst()
                        .orElseThrow(() -> new RuntimeException("Auth service unavailable"));
                String authUrl = "http://" + auth.host() + ":" + auth.port() + "/auth/token";
                ResponseEntity<String> authResp = new RestTemplate().exchange(authUrl, HttpMethod.POST, entity, String.class);

                if (!authResp.getStatusCode().is2xxSuccessful()) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
                }
            }

            // Forward to target service
            return new RestTemplate().exchange(targetUrl, HttpMethod.valueOf(method), entity, String.class);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * Checks public path with allowed HTTP method
     * Example in ServiceConfig: "/api/institutes:GET"
     */
    private boolean isPublicPath(String requestPath, String requestMethod, List<String> publicPaths) {
        if (publicPaths == null) return false;

        // remove trailing slash
        requestPath = requestPath.endsWith("/") && requestPath.length() > 1
                ? requestPath.substring(0, requestPath.length() - 1)
                : requestPath;

        for (String pathWithMethod : publicPaths) {
            String[] parts = pathWithMethod.split(":");
            String pathPattern = parts[0].endsWith("/") && parts[0].length() > 1
                    ? parts[0].substring(0, parts[0].length() - 1)
                    : parts[0];

            String allowedMethod = parts.length > 1 ? parts[1].toUpperCase() : null;

            // convert {var} to *
            pathPattern = pathPattern.replaceAll("\\{[^/]+}", "*");

            if (pathMatcher.match(pathPattern, requestPath)) {
                if (allowedMethod == null || allowedMethod.equalsIgnoreCase(requestMethod)) {
                    return true;
                }
            }
        }

        return false;
    }
}
