package com.gateway.registry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class RegistryService {

    private static final Logger log = LoggerFactory.getLogger(RegistryService.class);
    private final RedisTemplate<String, ServiceInfo> redisTemplate;

    /* This is a service registry contains all the services registered, provides operations for redis */
    public RegistryService(RedisTemplate<String, ServiceInfo> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /* Register service by service info */
    public void register(ServiceInfo service) {
        // creates a key with getKey method, requires service name will return for e.g., "registry: user-service"
        String key = getKey(service.name());
        try {
            redisTemplate.opsForList().rightPush(key, service);
            // expire or remove the stale data
            redisTemplate.expire(key, 1, TimeUnit.HOURS);
        } catch (Exception e) {
            log.error("Failed to register instance: {}", service, e);
        }
    }

    /* Deregister service by service info*/
    public void deregister(ServiceInfo service) {
        String key = getKey(service.name());
        /*
            opsForList provides operations such as push, remove...
            .remove requires three args
            key to be removed, how many occurrence to be removed and an object
        */
        try {
            redisTemplate.opsForList().remove(key, 1, service);
        } catch (Exception e) {
            log.error("Failed to deregister instance: {}", service, e);
        }
    }

    /* Gets the service */
    public Optional<ServiceInfo> getService(String serviceName) {
        String key = getKey(serviceName); // "registry:academic"
        try {
            ServiceInfo service = redisTemplate.opsForList().index(key, 0); // get first instance
            return Optional.ofNullable(service);
        } catch (Exception e) {
            log.error("Failed to get service: {}", serviceName, e);
            return Optional.empty();
        }
    }


    /* create a key, to be provided to the redis, "registry:user-service" */
    private String getKey(String service) {
        return "registry:" + service;
    }
}
