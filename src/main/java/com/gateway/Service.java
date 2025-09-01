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
    private final ServiceInfoRegistry serviceInfoRegistry;
    private final RouteValidator routeValidator;

    @Autowired
    public Service(ServiceInfoRegistry serviceInfoRegistry, RouteValidator routeValidator) {
        this.serviceInfoRegistry = serviceInfoRegistry;
        this.routeValidator = routeValidator;
    }

    /*
     * By providing HttpServlet incoming request url we can extract service name for redirection
     * */
    private String extractServiceNameFromUrl(String url) {
        if (url == null) {
            throw new IllegalArgumentException("URL cannot be null");
        }

        /*
          * Let's suppose the incoming url is like `localhost:4000/service-name/api/endpoint`
          * Extract service name from the url
          *
          * First convert the url into Array[] of streams
          * Split array by "/", so it will be like ["","/","service-name","endpoint"]
          *
          *
          * Remove the blanks ->  ["/","service-name","endpoint"]
          *
          *
          * Create new array
          * Our Service name will be at index 0, parts[0]
         */
        String[] parts = Arrays.stream(url.split("/"))
                .filter(s -> !s.isBlank())
                .toArray(String[]::new);

        if (parts.length == 0) {
            throw new IllegalArgumentException("No service name found in URL: " + url);
        }
        log.info("URL generated for service {}", parts[0]);
        return parts[0];
    }

    /*
     * Build target url from the existing request, http://localhost:8080/institutes/abc
     * */
    private String createTargetUrl(ServiceInfoRegistry.ServiceInfo serviceInfo, String url) {
        return "http://" + serviceInfo.getHost() + ":" + serviceInfo.getPort() + url;
    }

    /*
     * Helper method to find service info from in-memory YAML config
     * */
    private ServiceInfoRegistry.ServiceInfo getService(String serviceName) {
        return serviceInfoRegistry.getList()
                .stream()
                .filter(s -> s.getName().equals(serviceName))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Service Unavailable: " + serviceName));
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
             * serviceInfo will contain information such as name, host, port, routes in which routes exposure, method etc...
             * serviceName is received from the @extractServiceNameFromUrl
             * */
            String serviceName = extractServiceNameFromUrl(url);
            ServiceInfoRegistry.ServiceInfo serviceInfo = getService(serviceName);

            /*
             * Get the url incoming, but remember this url also contains service name so we have to remove that as well
             * Get the method type for e.g., GET, POST...
             */
            String urlSuffix = url.replaceFirst("/[^/]+", "");
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
             * Required url : http://host:port/urlSuffix
             */
            String targetUrl = createTargetUrl(serviceInfo, urlSuffix);
            log.info("Target Url Generated : {}", targetUrl);

            HttpEntity<byte[]> entity = new HttpEntity<>(body, headers);
            log.info("✅ Request Forwarded : {}", targetUrl);
            routeValidator.checkExposure(serviceName,urlSuffix);
            try {
                /*
                 * Forward the required request to the destination
                 */
                return restTemplate.exchange(targetUrl, method, entity, String.class);
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
