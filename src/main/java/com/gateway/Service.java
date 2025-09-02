package com.gateway;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletRequest;

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
     * forwardRequest redirect the incoming request from the client to the required
     * microservice
     * Few Args are required request, method, serviceName
     * Method contains GET, POST, PUT and DELETE as per REST API
     */
    public ResponseEntity<?> forwardRequest(HttpServletRequest request) {
        try {
            String url = request.getRequestURI();

            /*
             * Finds instance of the required service, firstly available if not return
             * Service Unavailable
             * service will contain information such as name, host, port, routes in which
             * routes exposure, method etc...
             * serviceName is received from the @extractServiceNameFromUrl
             */
            String serviceName = utils.extractServiceNameFromUrl(url);
            Registry.Service service = utils.getService(serviceName, registry);

            /*
             * @requestPath removes the service name from the url
             * 
             * @body read all content from the incoming request, create new one for
             * redirection
             * 
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
                 * 
                 * EXPOSURE: PUBLIC, Route request to the destination without any checks
                 * 
                 * EXPOSURE: PRIVATE, Request can only be made by internal network @isInternalIp
                 * 
                 * EXPOSURE: PROTECTED, Check if the incoming request contains token or not,
                 * authenticate this token with @isAuthenticated
                 */
                switch (exposure) {
                    case PUBLIC:
                        log.info("PUBLIC route accessed: {}", targetUrl);
                        return restTemplate.exchange(targetUrl, method, entity, String.class);

                    case PRIVATE:
                        String clientIp = request.getRemoteAddr();
                        if (utils.isInternalIp(clientIp)) {
                            log.info("PRIVATE route accessed from internal IP: {}", clientIp);
                            return restTemplate.exchange(targetUrl, method, entity, String.class);
                        } else {
                            log.warn("Blocked PRIVATE route request from external IP: {}", clientIp);
                            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                    .body("Access denied: PRIVATE route");
                        }

                    case PROTECTED:
                        if (utils.isAuthenticated(request, restTemplate, registry)) {
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
}
