package com.univault.gateway.gateway;

import com.univault.gateway.registry.InstanceInfo;
import com.univault.gateway.registry.RegistryService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GatewayService {
    private final RegistryService registryService;

    @Autowired
    public GatewayService(RegistryService registryService) {
        this.registryService = registryService;
    }
    /*
        create or resolves route for the incoming request
    */
    public String resolveRoute(String serviceName, String relativePath) {
        /*
            get the required service from the redis using @serviceName provided
            pick the first instance that is available
            build a url with provided args
        */
        List<InstanceInfo> instances = registryService.getInstances(serviceName);
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

            HttpEntity<String> entity = new HttpEntity<>(body);

            return restTemplate.exchange(
                    targetUrl,
                    HttpMethod.valueOf(method),
                    entity,
                    String.class
            );
        } catch (IOException e) {
            throw new RuntimeException("Error reading request body", e);
        }
    }
}
