package com.gateway.gateway;

import com.gateway.registry.RegistryService;
import com.gateway.registry.RouteInfo;
import com.gateway.registry.ServiceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/*
 * Already receiving urlSuffix containing /institutes/:id
 * Now need to check the exposure of the custom routes
 * They can be said which have custom configs such as different exposure settings for e.g., private
 * Only certain REST methods are allowed for example on /service-name/getAll, only GET is allowed
 * We have to also check for the dynamic variables defined in the routes
 * for e.g., /service-name/getAll/:id
 *
 *
 * First we find the info about called service
 * After this extract routes from the given service name
 * */
@Service
public class GatewayRouteValidator {

    private static final Logger log = LoggerFactory.getLogger(GatewayRouteValidator.class);
    private final RegistryService registryService;

    @Autowired
    public GatewayRouteValidator(RegistryService registryService){
        this.registryService = registryService;
    }

    public String checkExposure(String serviceName, String urlSuffix) {
        Optional<ServiceInfo> serviceInfo = registryService.getService(serviceName);
        log.info(String.valueOf(serviceInfo));
        List<RouteInfo> routes = serviceInfo
                .map(ServiceInfo::routes)
                .orElse(Collections.emptyList());

        for (RouteInfo r : routes) {
            if (r.path().equals(urlSuffix)) {
                System.out.println("Matched route: " + r);
                System.out.println("Exposure: " + r.exposure());
                return r.path();
            }
        }

        return "No match found"; // or return a default value / throw exception
    }

}
