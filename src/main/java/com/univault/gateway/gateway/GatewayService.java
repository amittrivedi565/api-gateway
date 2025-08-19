package com.univault.gateway.gateway;

import com.univault.gateway.registry.InstanceInfo;
import com.univault.gateway.registry.RegistryService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
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

    /*
       creates the complete routes according to requirement the service
       for e.g., endpoint of ims service is /api/institutes
    */
    public String resolveRoute(String serviceName, String relativePath) {
        /*
            get the required service from the redis using @serviceName provided
            pick the first instance that is available
            build a url with provided args
        */
        List<InstanceInfo> instances = registryService.getInstances(serviceName);

        if (instances == null || instances.isEmpty()) {
            log.error("No instance found for service: {}", serviceName);
            throw new RuntimeException("No instance found for service: " + serviceName);
        }

        InstanceInfo instance = instances.get(0);
        return "http://" + instance.host() + ":" + instance.port() + relativePath;
    }

    public ResponseEntity<?> forwardRequest(String targetUrl,
                                            HttpServletRequest request,
                                            String method) {
        RestTemplate restTemplate = new RestTemplate();

        try {
            String body = request.getReader()
                    .lines()
                    .collect(Collectors.joining(System.lineSeparator()));


            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(body,headers);

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
            return ResponseEntity.status(502).body("Error forwarding request: " + e.getMessage());
        }
    }

}
