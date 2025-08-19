package com.univault.gateway.registry;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class RegistryService {

    private final RedisTemplate<String, InstanceInfo> redisTemplate;

    // registry: service name → list of instances
    public RegistryService(RedisTemplate<String, InstanceInfo> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // Register service
    public void register(InstanceInfo instance) {
        // creates a key wit getKey method, requires service name will return for e.g., "registry: user-service"
        String key = getKey(instance.service());
        redisTemplate.opsForList().rightPush(key, instance);

        // expire or remove the stale data
        redisTemplate.expire(key, 5, TimeUnit.MINUTES);
    }

    // deregister service
    public void deregister(InstanceInfo instance) {
        String key = getKey(instance.service());
        /*
            opsForList provides operations such as push, remove...
            .remove requires three args
            key to be removed, how many occurrence to be removed and an object
        */
        redisTemplate.opsForList().remove(key,1,instance);
    }

    // get all the instances of a service
    public List<InstanceInfo> getInstances(String service) {
        String key = getKey(service);
        /*
            returns instances of the services, ranging between 0 and -1
        */
        return redisTemplate.opsForList().range(key,0,-1);
    }

    // create a key, to be provided to the redis, "registry:user-service"
    private String getKey(String service) {
        return "registry:" + service;
    }
}
