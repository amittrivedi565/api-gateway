package com.univault.gateway.config;

import com.univault.gateway.registry.InstanceInfo;
import com.univault.gateway.registry.RegistryService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ServiceRegistrar {

    private static final Logger log = LoggerFactory.getLogger(ServiceRegistrar.class);
    private final RegistryService registryService;
    private final ServiceConfig serviceConfig;

    @Autowired
    public ServiceRegistrar(RegistryService registryService, ServiceConfig serviceConfig) {
        this.registryService = registryService;
        this.serviceConfig = serviceConfig;
    }

    @PostConstruct
    public void registerOnStartup() {
        log.info("ServiceConfig list: {}", serviceConfig.getList());
        if (serviceConfig.getList().isEmpty()) {
            log.warn("No services found in configuration to register!");
            return;
        }

        for (ServiceConfig.ServiceInfo service : serviceConfig.getList()) {
            InstanceInfo instance = new InstanceInfo(
                    service.getName(),
                    service.getHost(),
                    service.getPort(),
                    service.getExposure(),
                    service.getPublicPaths()
            );
            log.info("Registering service: {}", instance);
            registryService.register(instance);
            log.info("✅ Auto-registered service: {}", instance);
        }
    }

}
