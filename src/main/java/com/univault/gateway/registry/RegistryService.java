package com.univault.gateway.registry;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class RegistryService {

    private static final Logger log = LoggerFactory.getLogger(RegistryService.class);
    private final RedisTemplate<String, InstanceInfo> redisTemplate;

    // registry: service name → list of instances
    public RegistryService(RedisTemplate<String, InstanceInfo> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // Register service
    public void register(InstanceInfo instance) {
        // creates a key with getKey method, requires service name will return for e.g., "registry: user-service"
        String key = getKey(instance.service());
        try {
            redisTemplate.opsForList().rightPush(key, instance);

            // expire or remove the stale data
            redisTemplate.expire(key, 5, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("Failed to register instance: {}", instance, e);
        }
    }

    // deregister service
    public void deregister(InstanceInfo instance) {
        String key = getKey(instance.service());
        /*
            opsForList provides operations such as push, remove...
            .remove requires three args
            key to be removed, how many occurrence to be removed and an object
        */
        try {
            redisTemplate.opsForList().remove(key, 1, instance);
        } catch (Exception e) {
            log.error("Failed to deregister instance: {}", instance, e);
        }
    }

    public InstanceInfo getInstance(String service) {
        List<InstanceInfo> instances = getInstances(service);
        return instances.isEmpty() ? null : instances.get(0);
    }

    // get all the instances of a service
    public List<InstanceInfo> getInstances(String service) {
        String key = getKey(service);
        /*
            returns instances of the services, ranging between 0 and -1
        */
        try {
            List<InstanceInfo> instances = redisTemplate.opsForList().range(key, 0, -1);
            return instances != null ? instances : List.of();
        } catch (Exception e) {
            log.error("Failed to get instances for service: {}", service, e);
            return List.of(); // return empty list if Redis fails
        }
    }

    // create a key, to be provided to the redis, "registry:user-service"
    private String getKey(String service) {
        return "registry:" + service;
    }
}
