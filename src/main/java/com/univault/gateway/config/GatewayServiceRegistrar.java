package com.univault.gateway.config;

import com.univault.gateway.registry.ServiceInfo;
import com.univault.gateway.registry.RegistryService;
import com.univault.gateway.registry.RouteInfo;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class GatewayServiceRegistrar {

    private static final Logger log = LoggerFactory.getLogger(GatewayServiceRegistrar.class);

    private final RegistryService registryService;
    private final GatewayServicesProperties gatewayServicesProperties;

    @Autowired
    public GatewayServiceRegistrar(RegistryService registryService, GatewayServicesProperties gatewayServicesProperties) {
        this.registryService = registryService;
        this.gatewayServicesProperties = gatewayServicesProperties;
    }

    /*
     * On application startup all the configurations defined in the application.yml will be registered
     * It will be stored and managed by registryService, specified for all the registry operations such as
     * Register, Deregister, getInstance, getInstances
     * */
    @PostConstruct
    public void registerOnStartup() {
        List<GatewayServicesProperties.ServiceInfo> services = gatewayServicesProperties.getList();
        log.info("Registering {} services from configuration", services.size());

        if (services.isEmpty()) {
            log.warn("No services found in configuration to register!");
            return;
        }

        for (GatewayServicesProperties.ServiceInfo service : services) {

            /*
                * RouteInfo :
                    path
                    method
                    exposure
            * */
            List<RouteInfo> routes = service.getRoutes().stream().map(r -> new RouteInfo(r.getPath(), r.getMethod(), r.getExposure())).collect(Collectors.toList());

            /*
                * ServiceInfo:
                    name
                    host
                    port
                    RouteInfo
            * */
            ServiceInfo instance = new ServiceInfo(
                    service.getName(),
                    service.getHost(),
                    service.getPort(), service.getDefaultExposure(), routes
            );
            log.info("Registering service instance: {}", instance);
            /* Service info saved in registry (redis) */
            registryService.register(instance);
            log.info("✅ Auto-registered service: {}", instance.name());
        }
    }
}
