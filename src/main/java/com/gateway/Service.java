package com.gateway;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;

@org.springframework.stereotype.Service
public class Service {

    private static final Logger log = LoggerFactory.getLogger(Service.class);

    private final RestTemplate restTemplate = new RestTemplate();
    private final Registry registry;
    private final Utils utils;
    private final Validator validator;

    @Autowired
    public Service(Registry registry, Utils utils, Validator validator) {
        this.registry = registry;
        this.utils = utils;
        this.validator = validator;
    }
    /*
     * forwardRequest redirect the incoming request from the client to the required microservice
     * Few Args are required request, method, serviceName
     * Method contains GET, POST, PUT and DELETE as per REST API
     * */
    public ResponseEntity<?> forwardRequest(HttpServletRequest request) {
        try {
            String url = request.getRequestURI();

            /*
             * Finds instance of the required service, firstly available if not return Service Unavailable
             * service will contain information such as name, host, port, routes in which routes exposure, method etc...
             * serviceName is received from the @extractServiceNameFromUrl
             * */
            String serviceName = utils.extractServiceNameFromUrl(url);
            Registry.Service service = utils.getService(serviceName, registry);


            /*
             * @requestPath removes the service name from the url
             * @body read all content from the incoming request, create new one for redirection
             * @method gets the method type for request e.g., GET, POST...
             */
            String requestPath = utils.removeServiceNameFromUrl(url);
            byte[] body = request.getInputStream().readAllBytes();
            HttpMethod method = HttpMethod.valueOf(request.getMethod());

            /*
             * Copy all the header coming with the request
             * Create a new request with header
             */
            HttpHeaders headers = new HttpHeaders();
            Collections.list(request.getHeaderNames())
                    .forEach(name -> headers.addAll(name, Collections.list(request.getHeaders(name))));

            /*
             * Incoming url : /academic-service/abc/123
             * Required url : http://host:port/routePath
             */
            String targetUrl = utils.createTargetUrl(service, requestPath);
            HttpEntity<byte[]> entity = new HttpEntity<>(body, headers);

            log.info("✅ Request Forwarded : {}", targetUrl);

            Registry.Exposure exposure = validator.checkExposure(serviceName, requestPath);

            try {
                /*
                 * Forward the required request to the destination
                 */
                switch (exposure) {
                    case PUBLIC:
                        log.info("PUBLIC route accessed: {}", targetUrl);
                        return restTemplate.exchange(targetUrl, method, entity, String.class);

                    case PRIVATE:
                        String clientIp = request.getRemoteAddr();
                        if (isInternalIp(clientIp)) {
                            log.info("PRIVATE route accessed from internal IP: {}", clientIp);
                            return restTemplate.exchange(targetUrl, method, entity, String.class);
                        } else {
                            log.warn("Blocked PRIVATE route request from external IP: {}", clientIp);
                            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                    .body("Access denied: PRIVATE route");
                        }

                    case PROTECTED:
                        if (isAuthenticated(request)) {
                            log.info("PROTECTED route accessed by authenticated user");
                            return restTemplate.exchange(targetUrl, method, entity, String.class);
                        } else {
                            log.warn("Blocked PROTECTED route: unauthenticated user");
                            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                    .body("Authentication required");
                        }

                    default:
                        log.error("Unknown exposure type: {}", exposure);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Unknown exposure type");
                }
            } catch (ResourceAccessException e) {

                /*
                 * If the service is down or not running, gracefully handle it
                 */
                log.error("Service {} is unavailable at {}: {}", serviceName, targetUrl, e.getMessage());
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body("Service " + serviceName + " is currently unavailable.");
            }
        } catch (Exception e) {
            log.error("Error forwarding request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }

    }

    private boolean isInternalIp(String ip) {
        return ip.startsWith("10.") || ip.startsWith("192.168.") || ip.startsWith("127.");
    }

    private boolean isAuthenticated(HttpServletRequest request) {
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
            if (token == null) return false;

            // 4. Call Auth Service to verify token
            Registry.Service service = registry.getList()
                    .stream()
                    .filter(s -> s.getName().equals("auth"))
                    .findFirst()
                    .orElseThrow();

            RestTemplate restTemplate = new RestTemplate();

            String authEndpoint = utils.createTargetUrl(service,"/auth/token");

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(authEndpoint, HttpMethod.POST, entity, String.class);

            return response.getStatusCode() == HttpStatus.OK;

        } catch (Exception e) {
            log.error("Error authenticating token", e);
            return false;
        }
    }


}
