package com.univault.gateway.registery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class RegistryService {

    // registry: service name → list of instances
    private final Map<String, List<InstanceInfo>> registry = new ConcurrentHashMap<>();

    // register new service
    public void register(InstanceInfo instance) {
        registry.computeIfAbsent(instance.service(), k -> new ArrayList<>())
                .add(instance);
    }

    // deregister service
    public void deregister(InstanceInfo instance) {
        registry.computeIfPresent(instance.service(), (service, list) -> {
            list.removeIf(i -> i.host().equals(instance.host()) && i.port() == instance.port());
            return list.isEmpty() ? null : list;
        });
    }

    // get all the instances of a service
    public List<InstanceInfo> getInstances(String service) {
        return registry.getOrDefault(service, Collections.emptyList());
    }
}
