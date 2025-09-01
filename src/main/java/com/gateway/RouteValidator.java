package com.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RouteValidator {

    private static final Logger log = LoggerFactory.getLogger(RouteValidator.class);
    private final ServiceInfoRegistry serviceInfoRegistry;

    public RouteValidator(ServiceInfoRegistry serviceInfoRegistry) {
        this.serviceInfoRegistry = serviceInfoRegistry;
    }

    /**
     * Returns the Exposure of the matched route.
     * If no specific route matches, returns the service's defaultExposure.
     * @param serviceName
     * @param urlSuffix
     */
    public ServiceInfoRegistry.Exposure checkExposure(String serviceName, String urlSuffix) {
        /**
         * First find the required service for which we are checking the exposure
         */
        ServiceInfoRegistry.ServiceInfo service = serviceInfoRegistry.getList()
                .stream()
                .filter(s -> s.getName().equals(serviceName))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Service not found: " + serviceName));

        /**
         * After fetching the service
         * Find the exposure of the incoming request
         * Return the exposure
         * ENUM -> PUBLIC, PRIVATE, PROTECTED
         */
        ServiceInfoRegistry.Exposure exposure = service.getRoutes()
                .stream()
                .filter(route -> route.getPath().equals(urlSuffix))
                .findFirst()
                .map(ServiceInfoRegistry.Route::getExposure)
                .orElse(service.getDefaultExposure());

        log.info("Exposure for service '{}' and route '{}': {}", serviceName, urlSuffix, exposure);

        return exposure;
    }
}
