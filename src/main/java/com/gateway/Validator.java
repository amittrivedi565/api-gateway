package com.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class Validator {

    private static final Logger log = LoggerFactory.getLogger(Validator.class);
    private final Registry registry;

    public Validator(Registry registry) {
        this.registry = registry;
    }

    /**
     * Returns the Exposure of the matched route.
     * If no specific route matches, returns the service's defaultExposure.
     */
    public Registry.Exposure checkExposure(String serviceName, String requestPath) {
        /*
         * First find the required service for which we are checking the exposure
         */
        Registry.Service service = registry.getList()
                .stream()
                .filter(s -> s.getName().equals(serviceName))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Service not found: " + serviceName));

        /*
         * After fetching the service
         * Find the exposure of the incoming request
         * Return the exposure
         * ENUM -> PUBLIC, PRIVATE, PROTECTED
         */
        Registry.Exposure exposure = service.getRoutes()
                .stream()
                .filter(route -> PatternMatcher(route.getPath(), requestPath))
                .findFirst()
                .map(Registry.Route::getExposure)
                .orElse(service.getDefaultExposure());

        log.info("Exposure for service '{}' and route '{}': {}", serviceName, requestPath, exposure);

        return exposure;
    }

    /*
     * PatterMatcher ensures that defined routes and requested route is same
     * This is required due to route being dynamic with variables such as /:id
     * This ensures that patterns are matched correctly
    */
    public Boolean PatternMatcher(String routePath, String requestPath){
        String[] routeParts = routePath.split("/");
        String[] requestParts = requestPath.split("/");

        if (routeParts.length != requestParts.length) {
            return false;
        }

        for(int i = 0; i < routeParts.length; i++){
            String routePart = routeParts[i];
            String requestPart = requestParts[i];

            if (routePart.isBlank() && requestPart.isBlank()) continue;

            if (routePart.startsWith(":")) {
                continue;
            }

            if (!routePart.equals(requestPart)) {
                return false;
            }

        }
        return true;
    }
}
