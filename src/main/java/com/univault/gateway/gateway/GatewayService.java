package com.univault.gateway.gateway;

import com.univault.gateway.registry.InstanceInfo;
import com.univault.gateway.registry.RegistryService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GatewayService {

    private static final Logger log = LoggerFactory.getLogger(GatewayService.class);

    private final RegistryService registryService;

    @Autowired
    public GatewayService(RegistryService registryService) {
        this.registryService = registryService;
    }

    /**
     * Resolve the full route for a service instance.
     */
    public String resolveRoute(String serviceName, String relativePath) {
        List<InstanceInfo> instances = registryService.getInstances(serviceName);

        if (instances.isEmpty()) {
            log.error("No instance found for service: {}", serviceName);
            throw new RuntimeException("No instance found for service: " + serviceName);
        }

        InstanceInfo instance = instances.get(0); // pick first instance
        return "http://" + instance.host() + ":" + instance.port() + relativePath;
    }

    /**
     * Forward the incoming request to the target service, with auth check for protected services.
     */
    public ResponseEntity<?> forwardRequest(String targetUrl,
                                            HttpServletRequest request,
                                            String method,
                                            String serviceName) {
        RestTemplate restTemplate = new RestTemplate();

        try {
            // Read request body
            String body = request.getReader()
                    .lines()
                    .collect(Collectors.joining(System.lineSeparator()));

            // Prepare headers and include token if present
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String token = request.getHeader("Authorization");
            if (token != null && !token.isEmpty()) {
                headers.set("Authorization", token);
            }

            HttpEntity<String> entity = new HttpEntity<>(body, headers);

            // Check if the target service is protected
            List<InstanceInfo> instances = registryService.getInstances(serviceName);
            if (!instances.isEmpty() && "protected".equalsIgnoreCase(instances.get(0).exposure())) {

                // Get auth-service instance
                List<InstanceInfo> authInstances = registryService.getInstances("auth-service");
                if (authInstances.isEmpty()) {
                    log.error("No auth-service instance available!");
                    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                            .body("Auth service unavailable");
                }

                InstanceInfo authInstance = authInstances.get(0);
                String authUrl = "http://" + authInstance.host() + ":" + authInstance.port() + "/auth" + "/token";

                // Call auth-service with token in headers
                HttpEntity<String> authEntity = new HttpEntity<>(body, headers);
                try {
                    ResponseEntity<String> authResponse = restTemplate.exchange(
                            authUrl,
                            HttpMethod.POST,
                            authEntity,
                            String.class
                    );

                    if (!authResponse.getStatusCode().is2xxSuccessful()) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body("Unauthorized: Auth service validation failed");
                    }

                } catch (HttpClientErrorException e) {
                    if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body("Unauthorized: Invalid token or credentials");
                    } else {
                        return ResponseEntity.status(e.getStatusCode())
                                .body("Error from auth-service: " + e.getResponseBodyAsString());
                    }
                } catch (HttpServerErrorException e) {
                    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                            .body("Auth service is currently unavailable");
                }
            }

            // Forward the request to the target service
            return restTemplate.exchange(
                    targetUrl,
                    HttpMethod.valueOf(method),
                    entity,
                    String.class
            );

        } catch (IllegalArgumentException e) {
            log.error("Invalid HTTP method: {}", method, e);
            return ResponseEntity.badRequest().body("Invalid HTTP method: " + method);
        } catch (IOException e) {
            log.error("Error reading request body", e);
            return ResponseEntity.internalServerError().body("Error reading request body");
        } catch (Exception e) {
            log.error("Error forwarding request to URL: {}", targetUrl, e);
            return ResponseEntity.status(502).body("Error forwarding request: ");
        }
    }
}
