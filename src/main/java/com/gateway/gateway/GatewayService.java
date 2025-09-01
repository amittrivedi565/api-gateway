package com.gateway.gateway;

import com.gateway.registry.RegistryService;
import com.gateway.registry.ServiceInfo;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;

@Service
public class GatewayService {

    private static final Logger log = LoggerFactory.getLogger(GatewayService.class);

    private final RestTemplate restTemplate = new RestTemplate();
    private final RegistryService registryService;
    private final GatewayRouteValidator gatewayRouteValidator;

    @Autowired
    public GatewayService(RegistryService registryService, GatewayRouteValidator gatewayRouteValidator) {
        this.registryService = registryService;
        this.gatewayRouteValidator = gatewayRouteValidator;
    }

    /*
     * By providing HttpServlet incoming request url we can extract service name for redirection
     * */
    private String extractServiceNameFromUrl(String url) throws NullPointerException {
        if (url == null) {
            throw new IllegalArgumentException("URL cannot be null");
        }
        /*
         * Url start with service name
         * Splits url with / in between for e.g., /academic/institutes/12354
         * After splitting -> parts = ["","academic","institutes","12345"]
         * Converts it to stream for perform filter operation
         * If the string is not empty then only add to the new String[] or don't
         * */
        String[] parts = Arrays.stream(url.split("/"))
                .filter(s -> !s.isBlank())
                .toArray(String[]::new);

        if (parts.length == 0) {
            throw new IllegalArgumentException("No service name found in URL: " + url);
        }
        log.info("URL generated for service {}", parts[0]);
        /* Returns service name*/
        return parts[0];
    }

    /*
     * Build target url from the existing request, http://localhost:8080/institutes/abc
     * */
    private String createTargetUrl(ServiceInfo serviceInfo, String url) {
        return "http://" + serviceInfo.host() + ":" + serviceInfo.port() + url;
    }

    /*
     * forwardRequest redirect the incoming request from the client to the required microservice
     * Few Args are required request, method, serviceName
     * Method contains GET, POST, PUT and DELETE as per REST API
     * */
    public ResponseEntity<?> forwardRequest(HttpServletRequest request){
        try {

            String url = request.getRequestURI();
            /*
             * Finds instance of the required service, firstly available if not return Service Unavailable
             * serviceInfo will contain information such as name, host, port, routes in which routes exposure, method etc...
             * serviceName is received from the @extractServiceNameFromUrl
             * */

            String serviceName = extractServiceNameFromUrl(url);
            ServiceInfo serviceInfo = registryService.getService(serviceName)
                    .orElseThrow(() -> new IllegalStateException("Service Unavailable: " + serviceName));

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
            * */

            HttpHeaders headers = new HttpHeaders();
            Collections.list(request.getHeaderNames())
                    .forEach(name -> headers.addAll(name,Collections.list(request.getHeaders(name))));


            /*
                * Incoming url : /academic-service/abc/123
                * Required url : http://host:port/urlSuffix
            * */
            String targetUrl = createTargetUrl(serviceInfo, urlSuffix);
            log.info("Target Url Generated : {}",targetUrl);

            HttpEntity<byte[]> entity = new HttpEntity<>(body, headers);
            log.info(urlSuffix);
            log.info("✅ Request Forwarded : {}",targetUrl);
            gatewayRouteValidator.checkExposure(serviceName,urlSuffix);
            return restTemplate.exchange(targetUrl, method, entity, String.class);

        } catch (Exception e) {
            log.error("Error forwarding request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
